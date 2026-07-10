package dev.jovanni0

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.repositories.UserRepository
import dev.jovanni0.routes.authRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*



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
        json()
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

    configureSerializer()
    configureAuth(jwt_config)

    val user_repository = UserRepository()

    configureRouting(
        jwt_config,
        user_repository,
    )
}
