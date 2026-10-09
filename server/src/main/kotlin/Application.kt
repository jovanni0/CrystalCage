package dev.jovanni0

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.auth.PasswordHasher
import dev.jovanni0.data_types.UserRole
import dev.jovanni0.database.DbConfig
import dev.jovanni0.database.migrateAllTenantSchemas
import dev.jovanni0.database.provisionTenantSchema
import dev.jovanni0.exceptions.*
import dev.jovanni0.exceptions.not_found.NotFoundException
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.filesystem.CoverThumbnails
import dev.jovanni0.repositories.*
import dev.jovanni0.routes.*
import dev.jovanni0.tables.User
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
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
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
    invite_code_repository: InviteCodeRepository,
    user_repository: UserRepository,
    author_repository: AuthorRepository,
    universe_repository: UniverseRepository,
    series_repository: SeriesRepository,
    book_repository: BookRepository,
    tag_repository: TagRepository,
    edition_repository: EditionRepository,
    cover_repository: CoverRepository,
    read_record_repository: ReadRecordRepository,
    tag_category_repository: TagCategoryRepository,
    http_client: HttpClient,
    cover_thumbnails: CoverThumbnails,
    read_progress_repository: ReadProgressRepository,
) {
    routing {
        serverRoutes()
        authRoutes(user_repository, invite_code_repository, jwt_config, data_source)
        authorRoutes(author_repository)
        bookRoutes(book_repository)
        coverRoutes(book_repository, cover_repository, cover_thumbnails)
        editionRoutes(book_repository, edition_repository)
        readRecordRoutes(read_record_repository)
        seriesRoutes(author_repository, universe_repository, series_repository)
        tagRoutes(tag_repository, tag_category_repository)
        universeRoutes(universe_repository)
        imageProxy(http_client)
        readProgressRoutes(read_progress_repository)
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

    migrateAllTenantSchemas(data_source)

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

        exception<InvalidInviteCodeException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest)
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


fun Application.seedAdminIfMissing(adminUsername: String, data_source: DataSource)
{
    var admin_id: String? = null

    transaction {
        val adminExists = User
            .selectAll()
            .where { User.role eq UserRole.ADMIN.toString() }
            .any()

        if (adminExists) return@transaction

        val plainPassword = environment.config.property("server.seed_admin_password").getString()

        val insert_statement = User.insert {
            it[username] = adminUsername
            it[password_hash] = PasswordHasher.hash(plainPassword)
            it[role] = UserRole.ADMIN.toString()
        }

        admin_id = insert_statement[User.id].toString()
    }

    if (admin_id != null)
    {
        provisionTenantSchema(data_source, admin_id)
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
    seedAdminIfMissing("admin", data_source)
    configureSerializer()
    configureAuth(jwt_config)
    configureStatusPages()
    configureCors()

    val storage_path = Path(environment.config.property("storage.baseDirectory").getString())
    val storage = AssetStorage(storage_path)
    val cover_thumbnails = CoverThumbnails(storage_path)

    val invite_code_repository = InviteCodeRepository()
    val user_repository = UserRepository(invite_code_repository)
    val author_repository = AuthorRepository()
    val universe_repository = UniverseRepository()
    val series_repository = SeriesRepository()
    val tag_repository = TagRepository()
    val edition_repository = EditionRepository(storage)
    val cover_repository = CoverRepository(storage)
    val book_repository = BookRepository(author_repository, cover_repository, edition_repository)
    val read_record_repository = ReadRecordRepository()
    val tag_category_repository = TagCategoryRepository()
    val read_progress_repository = ReadProgressRepository(read_record_repository)

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
        invite_code_repository,
        user_repository,
        author_repository,
        universe_repository,
        series_repository,
        book_repository,
        tag_repository,
        edition_repository,
        cover_repository,
        read_record_repository,
        tag_category_repository,
        http_client,
        cover_thumbnails,
        read_progress_repository,
    )
}
