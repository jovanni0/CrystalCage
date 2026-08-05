package routes

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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid



class AuthorRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val repo_mock = mockk<AuthorRepository>()
    private val username = "test-user"
    private val first_name = "john"
    private val last_name = "doe"

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



    @Nested
    inner class GetRoute
    {
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


    @Nested
    inner class PostRoute
    {
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
            } returns AuthorRow(Uuid.random(), first_name, last_name)

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
}
