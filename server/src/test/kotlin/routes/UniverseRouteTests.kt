package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.configureStatusPages
import dev.jovanni0.data_types.universe.request.UniverseCreateRequest
import dev.jovanni0.data_types.universe.response.UniverseResponse
import dev.jovanni0.exceptions.not_found.AuthorNotFoundException
import dev.jovanni0.records.universe.UniverseRow
import dev.jovanni0.repositories.UniverseRepository
import dev.jovanni0.routes.universeRoutes
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
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
import io.ktor.util.AttributeKey
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid

class UniverseRouteTests
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


    fun testUniverseRoute(repo: UniverseRepository, authorize: Boolean = true, block: suspend (HttpClient) -> Unit) =
        testApplication()
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

                install(DefaultRequest.Plugin)
                {
                    if (authorize)
                    {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }
                }
            }

            block(client)
        }



    @Nested
    inner class GetRoute
    {
        @Test
        fun `return 200 ok and a list for existing universe`()
        {
            val author_uuid = Uuid.random()
            val db_rows = listOf(
                UniverseRow(Uuid.random(), author_uuid, "Murderbot"),
            )

            every { repo_mock.findAllByAuthorId("user_$username", author_uuid) } returns db_rows

            testUniverseRoute(repo_mock)
            { client ->
                val response = client.get("/universes?author_id=$author_uuid")

                assertEquals(HttpStatusCode.OK, response.status)

                val body = response.body<List<UniverseResponse>>()

                assertEquals(db_rows.size, body.size)
                assertEquals(db_rows[0].name, body[0].name)
            }
        }


        @Test
        fun `return 400 bad request when author_id param is missing`()
        {
            testUniverseRoute(repo_mock)
            { client ->
                val response = client.get("/universes")

                assertEquals(HttpStatusCode.BadRequest, response.status)
                assertEquals("author_id is required but was not provided", response.bodyAsText())
            }

            verify { repo_mock wasNot Called }
        }


        @Test
        fun `return 400 bad request when author_id param is empty`()
        {
            testUniverseRoute(repo_mock)
            { client ->
                val response = client.get("/universes?author_id=")

                assertEquals(HttpStatusCode.BadRequest, response.status)
                assertEquals("author_id is not a valid UUID", response.bodyAsText())
            }

            verify { repo_mock wasNot Called }
        }


        @Test
        fun `return 401 unauthorized when no token is provided`()
        {
            testUniverseRoute(repo_mock, false)
            { client ->
                val response = client.get("/universes?author_id=${Uuid.random()}")

                assertEquals(HttpStatusCode.Unauthorized, response.status)
            }

            verify { repo_mock wasNot Called }
        }
    }



    @Nested
    inner class PostRoute
    {
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

                verify { repo_mock wasNot Called }
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
}
