package routes.author

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.data_types.author.AuthorResponse
import dev.jovanni0.records.author.AuthorRow
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.routes.authorRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid



class GetRouteTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val repo_mock = mockk<AuthorRepository>()
    private val username = "test-user"

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testAuthorRoute(repo: AuthorRepository, block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
        }

        routing {
            authorRoutes(repo)
        }

        val client = createClient {
            install(ContentNegotiation) { json() }

            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        block(client)
    }


    @Test
    fun `returns 200 ok with a list of authors`()
    {
        val uuid_1 = Uuid.random()
        val db_rows = listOf(
            AuthorRow(uuid_1, "John", "Doe"),
            AuthorRow(Uuid.random(), "Jane", "Doe"),
        )

        every { repo_mock.findAll("user_$username") } returns db_rows

        testAuthorRoute(repo_mock)
        { client ->
            val response = client.get("/authors")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<AuthorResponse>>()

            assertEquals(2, body.size)
            assertEquals(uuid_1.toString(), body[0].id)
            assertEquals("Jane", body[1].first_name)
        }
    }


    @Test
    fun `return 200 ok and empty list when no authors exist`()
    {
        every { repo_mock.findAll("user_$username") } returns listOf()

        testAuthorRoute(repo_mock)
        { client ->
            val response = client.get("/authors")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<AuthorResponse>>()

            assertEquals(0, body.size)
        }
    }
}
