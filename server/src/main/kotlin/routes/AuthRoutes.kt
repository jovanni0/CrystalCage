package dev.jovanni0.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.auth.PasswordHasher
import dev.jovanni0.data_types.UserRole
import dev.jovanni0.data_types.auth.InviteCreateResponse
import dev.jovanni0.data_types.auth.LoginRequest
import dev.jovanni0.data_types.auth.LoginResponse
import dev.jovanni0.data_types.auth.MeResponse
import dev.jovanni0.data_types.auth.SignupRequest
import dev.jovanni0.database.provisionTenantSchema
import dev.jovanni0.repositories.InviteCodeRepository
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
import io.ktor.server.util.getOrFail
import io.ktor.util.toLowerCasePreservingASCIIRules
import io.ktor.util.toUpperCasePreservingASCIIRules
import java.util.Date
import javax.sql.DataSource


fun Route.authRoutes(
    user_repository: UserRepository,
    invite_code_repository: InviteCodeRepository,
    jwt_config: AuthConfig,
    data_source: DataSource,
)
{
    get("/auth/invite-code/{code}")
    {
        val code_raw = call.pathParameters.get("code") ?: return@get call.respond(HttpStatusCode.BadRequest, "missing code")

        val code = code_raw.trim().replace("-", "").toUpperCasePreservingASCIIRules()

        if (code.length != 8)
        {
            return@get call.respond(HttpStatusCode.BadRequest, "invalid code")
        }

        val is_valid = invite_code_repository.isValid(code)

        call.respond(is_valid)
    }


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
            .withClaim("user_id", user.id.toString())
            .withExpiresAt(Date(System.currentTimeMillis() + 84_400_000))
            .sign(Algorithm.HMAC256(jwt_config.secret))

        call.respond(
            LoginResponse(
                id = user.id.toString(),
                token = token
            )
        )
    }


    post("/auth/signup")
    {
        val request = call.receive<SignupRequest>()

        if (!request.validate())
        {
            call.respond(HttpStatusCode.BadRequest, "fields are blank or password is too short")
            return@post
        }

        val code = request.code.trim().replace("-", "").toUpperCasePreservingASCIIRules()

        if (code.length != 8)
        {
            return@post call.respond(HttpStatusCode.BadRequest, "invalid code")
        }

        val password_hash = PasswordHasher.hash(request.password)
        val result = user_repository.create(code, request.username, password_hash, UserRole.USER)

        if (result == null)
        {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        try
        {
            provisionTenantSchema(data_source, result.id.toString())
        }
        catch(e: Throwable)
        {
            user_repository.delete(result.id)
            return@post call.respond(HttpStatusCode.InternalServerError)
        }

        call.respond(HttpStatusCode.Created)
    }


    /**
     * get the user's details.
     */
    authenticate("auth-jwt")
    {
        post("/auth/invite-code")
        {
            val principal = call.principal<JWTPrincipal>()
            val user_id = principal!!.payload.getClaim("user_id").asString()

            val user = user_repository.findById(user_id)

            if (user == null || UserRole.valueOf(user.role) != UserRole.ADMIN)
            {
                return@post call.respond(HttpStatusCode.Unauthorized)
            }

            val code = invite_code_repository.generate(user.id, null)

            if (code == null)
            {
                return@post call.respond(HttpStatusCode.InternalServerError, "could not generate code")
            }

            call.respond(HttpStatusCode.Created, InviteCreateResponse(code))
        }


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
                    id = user.id.toString(),
                    username = user.username,
                    role = user.role,
                    created_at = user.created_at
                )
            )
        }
    }
}
