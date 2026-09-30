package database

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid



class TenantResolutionTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )


    /**
     * generates a valid JWT token for the given user.
     */
    private fun generateToken(user_id: String?): String
    {
        return JWT
            .create()
            .withIssuer(jwt_config.issuer)
            .withAudience(jwt_config.audience)
            .withClaim("realm", jwt_config.realm)
            .apply {
                if(user_id != null)
                {
                    withClaim("user_id", user_id)
                }
            }
            .sign(Algorithm.HMAC256(jwt_config.secret))
    }


    /**
     * sets up the routing for the test, including auth and tenant resolution.
     */
    fun TestApplicationBuilder.setupTenantResolutionRoute(jwt_config: AuthConfig)
    {
        application {
            configureAuth(jwt_config)

            routing {
                authenticate("auth-jwt") {
                    install(TenantResolution)

                    get("/tenant-resolution") {
                        call.respondText(call.tenant_schema)
                    }
                }
            }
        }
    }


    @Test
    fun `resolves valid user_id claim to tenant_schema attribute`() = testApplication()
    {
        val user_id = "d7edbc1c-bc58-4feb-8b68-2965c0788a5d"
        val token = generateToken(user_id)
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")
        {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("user_$user_id", response.bodyAsText())
    }


    @Test
    fun `aborts request with 401 when user_id claim is missing`() = testApplication()
    {
        val token = generateToken(null)
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")
        {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("", response.bodyAsText())
    }


    @Test
    fun `aborts request with 401 when principal is completely absent`() = testApplication()
    {
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals("", response.bodyAsText())
    }


    @Test
    fun `abort request with 401 when user_id has invalid SQL characters`() = testApplication()
    {
        val user_id = "id-123.with spaces;'\"--;|/*\u0000"
        val token = generateToken(user_id)
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")
        {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `abort request with 401 when user_id is empty`() = testApplication()
    {
        val user_id = ""
        val token = generateToken(user_id)
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")
        {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    @Test
    fun `abort request with 401 when user_id is blank characters`() = testApplication()
    {
        val user_id = " "
        val token = generateToken(user_id)
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")
        {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }


    /**
     * PostgreSQL has a default identifier limit of 63 bytes.
     */
    @Test
    fun `abort request with 401 when user_id is longer then 45 characters`() = testApplication()
    {
        val user_id = "tests".repeat(10)
        val token = generateToken(user_id)
        setupTenantResolutionRoute(jwt_config)

        val response = client.get("/tenant-resolution")
        {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
