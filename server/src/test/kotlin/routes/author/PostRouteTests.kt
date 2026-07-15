package routes.author

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.data_types.author.AuthorCreateRequest
import dev.jovanni0.data_types.author.AuthorResponse
import dev.jovanni0.records.author.AuthorRow
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.routes.authorRoutes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull



class PostRouteTests
{
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
        .withClaim("user_id", "test-user")
        .sign(Algorithm.HMAC256(jwt_config.secret))

    private val repo_mock = mockk<AuthorRepository>()
    private val first_name = "john"
    private val last_name = "doe"


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
    fun `returns 400 bad request when last_name is blank`()
    {
        testAuthorRoute(repo_mock)
        { client ->
            val response = client.post("/authors")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(AuthorCreateRequest(first_name, "  "))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("last_name is required", response.bodyAsText())

            verify { repo_mock wasNot Called}
        }
    }

    @Test
    fun `returns 400 bad request when last_name is empty`()
    {
        testAuthorRoute(repo_mock)
        { client ->
            val response = client.post("/authors")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(AuthorCreateRequest(first_name, ""))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertEquals("last_name is required", response.bodyAsText())

            verify { repo_mock wasNot Called}
        }
    }


    @Test
    fun `returns 400 bad request when last_name is missing`()
    {
        testAuthorRoute(repo_mock)
        { client ->
            val response = client.post("/authors")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("{ \"first_name\": \"$first_name\" }")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)

            verify { repo_mock wasNot Called}
        }
    }


    @Test
    fun `returns 400 bad request for invalid json`()
    {
        testAuthorRoute(repo_mock)
        { client ->
            val response = client.post("/authors")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(" \"first_name\": \"$first_name\" ")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)

            verify { repo_mock wasNot Called}
        }
    }


    @Test
    fun `returns 201 created when proper request is sent`()
    {
        every()
        {
            repo_mock.create("user_test-user", first_name, last_name)
        } returns AuthorRow("uuid-1", first_name, last_name)

        testAuthorRoute(repo_mock)
        { client ->
            val response = client.post("/authors")
            {
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(AuthorCreateRequest(first_name, last_name))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            assertNotNull(response.body<AuthorResponse>().id)
        }
    }
}
