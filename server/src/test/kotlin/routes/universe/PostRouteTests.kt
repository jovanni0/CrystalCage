package routes.universe

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.configureStatusPages
import dev.jovanni0.data_types.universe.request.UniverseCreateRequest
import dev.jovanni0.exceptions.not_found.AuthorNotFoundException
import dev.jovanni0.repositories.UniverseRepository
import dev.jovanni0.routes.universeRoutes
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.util.AttributeKey
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid



class PostRouteTests
{
    private val SkipAuth = AttributeKey<Boolean>("SkipAuth")
    private val repo_mock = mockk<UniverseRepository>()
    private val username = "test-user"

    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testUniverseRoute(repo: UniverseRepository, authorize: Boolean = true, block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            universeRoutes(repo_mock)
        }

        val client = createClient {
            install(ContentNegotiation) { json() }

            install(DefaultRequest)
            {
                if (authorize)
                {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            }
        }

        block(client)
    }


    @Test
    fun `returns 400 bad request for invalid json`()
    {
        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(" some invalid json ")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)

            verify { repo_mock wasNot Called}
        }
    }


    @Test
    fun `return 401 unauthorized when no token is provided`()
    {
        testUniverseRoute(repo_mock, false)
        { client ->
            val response = client.post("/universes")
            {
                attributes.put(SkipAuth, true)
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(UniverseCreateRequest(Uuid.random().toString(), "Tester"))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }

        verify { repo_mock wasNot Called }
    }


    @Test
    fun `return 400 bad request for a bad author_id uuid`()
    {
        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(UniverseCreateRequest("uuid-here", "Tester"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        verify { repo_mock wasNot Called }
    }


    @Test
    fun `return 400 bad request for a blank author_id`()
    {
        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(UniverseCreateRequest("  ", "Tester"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        verify { repo_mock wasNot Called }
    }


    @Test
    fun `return 400 bad request for a missing author_id`()
    {
        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{ \"name\": \"Tester\" }")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        verify { repo_mock wasNot Called }
    }


    @Test
    fun `return 404 not found when author does not exist`()
    {
        val name = "Tester"
        val author_id = Uuid.random()

        every {
            repo_mock.create("user_$username", name, author_id)
        } throws AuthorNotFoundException(author_id.toString())

        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(UniverseCreateRequest(author_id.toString(), name))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals("author $author_id does not exist", response.bodyAsText())
        }
    }


    @Test
    fun `return 400 bad request for a blank name`()
    {
        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(UniverseCreateRequest(Uuid.random().toString(), "  "))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("name cannot be blank", response.bodyAsText())
        }

        verify { repo_mock wasNot Called }
    }


    @Test
    fun `return 400 bad request for a missing name`()
    {
        testUniverseRoute(repo_mock)
        { client ->
            val response = client.post("/universes")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{ \"author_id\": \"${Uuid.random()}\" }")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        verify { repo_mock wasNot Called }
    }
}
