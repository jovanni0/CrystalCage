package dev.jovanni0.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.auth.PasswordHasher
import dev.jovanni0.data_types.UserRole
import dev.jovanni0.data_types.auth.LoginRequest
import dev.jovanni0.data_types.auth.LoginResponse
import dev.jovanni0.data_types.auth.MeResponse
import dev.jovanni0.database.provisionTenantSchema
import dev.jovanni0.repositories.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.Date
import javax.sql.DataSource


fun Route.authRoutes(
    user_repository: UserRepository,
    jwt_config: AuthConfig,
    data_source: DataSource,
)
{
    /**
     * authenticate the user.
     */
    post("/auth/login")
    {
        val request = call.receive<LoginRequest>()
        val user = user_repository.findByUsername(request.username)

        if (user == null)
        {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val is_password_valid = PasswordHasher.doesMatch(request.password, user.password_hash)

        if (!is_password_valid)
        {
            call.respond(HttpStatusCode.Unauthorized)
            return@post
        }

        val token = JWT
            .create()
            .withIssuer(jwt_config.issuer)
            .withAudience(jwt_config.audience)
            .withClaim("realm", jwt_config.realm)
            .withClaim("user_id", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 84_400_000))
            .sign(Algorithm.HMAC256(jwt_config.secret))

        call.respond(
            LoginResponse(
                id = user.id,
                token = token
            )
        )
    }


    post("/auth/signup")
    {
        val request = call.receive<LoginRequest>()

        if (!request.validate())
        {
            call.respond(HttpStatusCode.BadRequest, "fields are blank or password is too short")
            return@post
        }

        val password_hash = PasswordHasher.hash(request.password)
        val result = user_repository.create(request.username, password_hash, UserRole.ADMIN)

        if (result == null)
        {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        provisionTenantSchema(data_source, result.id)

        call.respond(HttpStatusCode.Created)
    }


    /**
     * get the user's details.
     */
    authenticate("auth-jwt")
    {
        get("/auth/me")
        {
            val principal = call.principal<JWTPrincipal>()
            val user_id = principal!!.payload.getClaim("user_id").asString()
            val user = user_repository.findById(user_id)

            if (user == null)
            {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            call.respond(
                MeResponse(
                    id = user.id,
                    username = user.username,
                    role = user.role,
                    created_at = user.created_at
                )
            )
        }
    }
}
