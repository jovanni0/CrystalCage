package dev.jovanni0

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.database.DbConfig
import dev.jovanni0.exceptions.ConfigurationException
import dev.jovanni0.exceptions.ConflictException
import dev.jovanni0.exceptions.NotFoundException
import dev.jovanni0.repositories.UserRepository
import dev.jovanni0.routes.authRoutes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.jdbc.Database



/**
 * handles request authentication on protected routes. rejects requests that do not have a valid claim token.
 */
fun Application.configureAuth(jwt_config: AuthConfig)
{
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

                if (realm == jwt_config.realm && user_id != null)
                {
                    JWTPrincipal(it.payload)
                }
                else
                {
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
    user_repository: UserRepository,
)
{
    routing {
        authRoutes(user_repository, jwt_config)
    }
}



/**
 * install JSON request/response serialization.
 */
fun Application.configureSerializer()
{

    install(ContentNegotiation) {
        // Json's `encodeDefaults` defaults to `false`; this is relied on to keep
        // `Patch.Undefined` fields from ever being serialized if such a DTO is
        // echoed back in a response.
        json()
    }
}


fun Application.configureDatabase(): HikariDataSource
{
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
fun Application.configureStatusPages()
{
    install(StatusPages)
    {
        exception<NotFoundException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, cause.message)
        }

        exception<ConflictException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, cause.message)
        }

        exception<ConfigurationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message)
        }

        exception<ExposedSQLException> { call, cause ->
            when (cause.sqlState)
            {
                "23503" -> call.respond(HttpStatusCode.BadRequest, "referenced entity does not exist")
                else -> call.respond(HttpStatusCode.InternalServerError, "database error")
            }
        }
    }
}



/**
 * application entry point.
 */
fun Application.module()
{
    val jwt_config = AuthConfig(
        environment.config.property("jwt.secret").getString(),
        environment.config.property("jwt.issuer").getString(),
        environment.config.property("jwt.audience").getString(),
        environment.config.property("jwt.realm").getString()
    )
    jwt_config.validate()

    configureDatabase()
    configureSerializer()
    configureAuth(jwt_config)
    configureStatusPages()

    val user_repository = UserRepository()

    configureRouting(
        jwt_config,
        user_repository,
    )
}
