package dev.jovanni0

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.database.DbConfig
import dev.jovanni0.exceptions.*
import dev.jovanni0.exceptions.not_found.NotFoundException
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.repositories.*
import dev.jovanni0.routes.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import javax.sql.DataSource
import kotlin.io.path.Path


/**
 * handles request authentication on protected routes. rejects requests that do not have a valid claim token.
 */
fun Application.configureAuth(jwt_config: AuthConfig) {
    val jwt_verifier = JWT
        .require(Algorithm.HMAC256(jwt_config.secret))
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .build()

    install(Authentication)
    {
        jwt("auth-jwt")
        {
            realm = jwt_config.realm

            verifier(jwt_verifier)
            validate {
                val realm = it.payload.getClaim("realm").asString()
                val user_id = it.payload.getClaim("user_id").asString()

                if (realm == jwt_config.realm && user_id != null) {
                    JWTPrincipal(it.payload)
                } else {
                    null
                }
            }
        }
    }
}


/**
 * set up the routes.
 */
fun Application.configureRouting(
    jwt_config: AuthConfig,
    data_source: DataSource,
    user_repository: UserRepository,
    author_repository: AuthorRepository,
    universe_repository: UniverseRepository,
    series_repository: SeriesRepository,
    book_repository: BookRepository,
    tag_repository: TagRepository,
    edition_repository: EditionRepository,
    cover_repository: CoverRepository,
    read_record_repository: ReadRecordRepository,
    http_client: HttpClient,
) {
    routing {
        serverRoutes()
        authRoutes(user_repository, jwt_config, data_source)
        authorRoutes(author_repository)
        bookRoutes(book_repository)
        coverRoutes(book_repository, cover_repository)
        editionRoutes(book_repository, edition_repository)
        readRecordRoutes(read_record_repository)
        seriesRoutes(author_repository, universe_repository, series_repository)
        tagRoutes(tag_repository)
        universeRoutes(universe_repository)
        imageProxy(http_client)
    }
}


/**
 * install JSON request/response serialization.
 */
fun Application.configureSerializer() {

    install(ContentNegotiation) {
        // Json's `encodeDefaults` defaults to `false`; this is relied on to keep
        // `Patch.Undefined` fields from ever being serialized if such a DTO is
        // echoed back in a response.
        json()
    }
}


fun Application.configureDatabase(): HikariDataSource {
    val db_config = DbConfig(
        jdbc_url = environment.config.property("database.jdbc_url").getString(),
        username = environment.config.property("database.username").getString(),
        password = environment.config.property("database.password").getString(),
    )
    db_config.validate()

    val hikari_config = HikariConfig().apply {
        jdbcUrl = db_config.jdbc_url
        username = db_config.username
        password = db_config.password
        maximumPoolSize = 10
        schema = "public" // see server issues # connection pooling and `search_path` leakage
    }

    val data_source = HikariDataSource(hikari_config)

    Flyway
        .configure()
        .dataSource(data_source)
        .locations("classpath:db/migration/shared")
        .load()
        .migrate()

    Database.connect(data_source)

    return data_source
}


/**
 * register exception handlers.
 */
fun Application.configureStatusPages() {
    install(StatusPages)
    {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message)
        }

        exception<AssetStorageException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message)
        }

        exception<AuthorMismatchException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message)
        }

        exception<ConfigurationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message)
        }

        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message)
        }

        exception<PathTooLongException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.message)
        }

        exception<ExposedSQLException> { call, cause ->
            when (cause.sqlState) {
                "23503" -> call.respond(HttpStatusCode.BadRequest, "referenced entity does not exist")
//                else -> call.respond(HttpStatusCode.InternalServerError, "database error")
            }
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Unknown error")
        }
    }
}


fun Application.configureCors() {

    install(CORS)
    {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
    }
}


/**
 * application entry point.
 */
fun Application.module() {
    val jwt_config = AuthConfig(
        environment.config.property("jwt.secret").getString(),
        environment.config.property("jwt.issuer").getString(),
        environment.config.property("jwt.audience").getString(),
        environment.config.property("jwt.realm").getString()
    )
    jwt_config.validate()

    val data_source = configureDatabase()
    configureSerializer()
    configureAuth(jwt_config)
    configureStatusPages()
    configureCors()

    val storage = AssetStorage(Path("./crystalcage"))

    val user_repository = UserRepository()
    val author_repository = AuthorRepository()
    val universe_repository = UniverseRepository()
    val series_repository = SeriesRepository()
    val tag_repository = TagRepository()
    val edition_repository = EditionRepository(storage)
    val cover_repository = CoverRepository(storage)
    val book_repository = BookRepository(author_repository, cover_repository, edition_repository)
    val read_record_repository = ReadRecordRepository()

    val http_client = HttpClient(CIO)
    {
        expectSuccess = true

        install(HttpTimeout)
        {
            connectTimeoutMillis = 5000
            requestTimeoutMillis = 15000
        }
    }

    configureRouting(
        jwt_config,
        data_source,
        user_repository,
        author_repository,
        universe_repository,
        series_repository,
        book_repository,
        tag_repository,
        edition_repository,
        cover_repository,
        read_record_repository,
        http_client,
    )
}
