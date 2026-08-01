package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.data_types.book.BookSummaryResponse
import dev.jovanni0.data_types.series.SeriesCreateRequest
import dev.jovanni0.data_types.series.SeriesDetailResponse
import dev.jovanni0.data_types.series.SeriesResponse
import dev.jovanni0.records.series.SeriesRow
import dev.jovanni0.records.universe.UniverseRow
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.SeriesRepository
import dev.jovanni0.repositories.UniverseRepository
import dev.jovanni0.routes.seriesRoutes
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
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.Uuid



class SeriesRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val series_repo_mock = mockk<SeriesRepository>()
    private val author_repo_mock = mockk<AuthorRepository>()
    private val universe_repo_mock = mockk<UniverseRepository>()
    private val username = "test-user"

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testSeriesRoute(block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
        }

        routing {
            seriesRoutes(author_repo_mock, universe_repo_mock, series_repo_mock)
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
    inner class GetSeries
    {
        @Test
        fun `no filters returns all series with 200`()
        {
            testSeriesRoute { client ->
                val expected = listOf(SeriesRow(Uuid.random(), Uuid.random(), null, "Series A", false))
                every {
                    series_repo_mock.findAllByAuthorIdAndUniverseId(any(), null, null)
                } returns expected

                val response = client.get("/series")

                assertEquals(HttpStatusCode.OK, response.status)
                verify(exactly = 1)
                {
                    series_repo_mock.findAllByAuthorIdAndUniverseId(any(), null, null)
                }
            }
        }


        @Test
        fun `valid author_id is parsed and passed to repo`()
        {
            testSeriesRoute { client ->
                val author_id = Uuid.random()
                every {
                    series_repo_mock.findAllByAuthorIdAndUniverseId(any(), author_id, null)
                } returns emptyList()

                val response = client.get("/series?author_id=$author_id")

                assertEquals(HttpStatusCode.OK, response.status)
                verify {
                    series_repo_mock.findAllByAuthorIdAndUniverseId(any(), author_id, null)
                }
            }
        }


        @Test
        fun `invalid author_id returns 400 and never calls repo`()
        {
            testSeriesRoute { client ->
                val response = client.get("/series?author_id=not-a-uuid")

                assertEquals(HttpStatusCode.BadRequest, response.status)
                verify(exactly = 0)
                {
                    series_repo_mock.findAllByAuthorIdAndUniverseId(any(), any(), any())
                }
            }
        }


        @Test
        fun `invalid universe_id returns 400 and never calls repo`()
        {
            testSeriesRoute { client ->
                val response = client.get("/series?universe_id=not-a-uuid")

                assertEquals(HttpStatusCode.BadRequest, response.status)
                verify(exactly = 0)
                {
                    series_repo_mock.findAllByAuthorIdAndUniverseId(any(), any(), any())
                }
            }
        }


        @Test
        fun `both filters invalid returns 400 for author_id first`()
        {
            testSeriesRoute { client ->
                val response = client.get("/series?author_id=bad&universe_id=alsobad")

                assertEquals(HttpStatusCode.BadRequest, response.status)
                assertTrue(response.bodyAsText().contains("author_id"))
            }
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
            }
            routing {
                seriesRoutes(author_repo_mock, universe_repo_mock, series_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/series")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                series_repo_mock.findAllByAuthorIdAndUniverseId(any(), any(), any())
            }
        }
    }



    @Nested
    inner class GetSeriesById
    {
        @Test
        fun `existing id returns 200 with body`()
        {
            testSeriesRoute { client ->
                val id = Uuid.random()
                val detail = SeriesDetailResponse(id.toString(), Uuid.random().toString(), null, "Series", false, emptyList())
                every {
                    series_repo_mock.findById(any(), id)
                } returns detail

                val response = client.get("/series/$id")

                assertEquals(HttpStatusCode.OK, response.status)

                val body = response.body<SeriesDetailResponse>()

                assertEquals(id.toString(), body.id)
                assertEquals("Series", body.name)
                assertEquals(emptyList<BookSummaryResponse>(), body.books)
            }
        }


        @Test
        fun `non-existent id returns 404`() = testSeriesRoute()
        { client ->
            val id = Uuid.random()
            every {
                series_repo_mock.findById(any(), id)
            } returns null

            val response = client.get("/series/$id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id returns 400 and never calls repo`() = testSeriesRoute()
        { client ->
            val response = client.get("/series/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                series_repo_mock.findById(any(), any())
            }
        }
    }



    @Nested
    inner class PostSeries
    {
        @Test
        fun `valid payload with universe_id returns 201`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            val universe_id = Uuid.random()
            val universe_record = UniverseRow(universe_id, author_id, "Name")

            every { author_repo_mock.exists(any(), author_id) } returns true
            every { universe_repo_mock.exists(any(), universe_id) } returns true
            every { universe_repo_mock.findById(any(), universe_id) } returns universe_record
            every { series_repo_mock.create(any(), author_id, universe_id, "My Series", true) } returns
                    SeriesRow(Uuid.random(), author_id, universe_id, "My Series", true)

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"$author_id","universe_id":"$universe_id","name":"My Series","is_finished":true}""")
            }

            assertEquals(HttpStatusCode.Created, response.status)

            val body = response.body<SeriesResponse>()

            assertEquals("My Series", body.name)
        }


        @Test
        fun `valid payload without universe_id returns 201 with null universe_id`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            every { author_repo_mock.exists(any(), author_id) } returns true
            every { series_repo_mock.create(any(), author_id, null, "My Series", false) } returns
                    SeriesRow(Uuid.random(), author_id, null, "My Series", false)

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody(SeriesCreateRequest(author_id.toString(), null, "My Series", false))
            }

            println(response.bodyAsText())

            assertEquals(HttpStatusCode.Created, response.status)
        }


        @Test
        fun `blank name returns 400 and never calls repo`() = testSeriesRoute()
        { client ->
            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"${Uuid.random()}","name":"   "}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                series_repo_mock.create(any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `malformed author_id returns 400`() = testSeriesRoute()
        { client ->
            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"not-a-uuid","name":"Series"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `malformed universe_id returns 400`() = testSeriesRoute()
        { client ->
            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"${Uuid.random()}","universe_id":"not-a-uuid","name":"Series"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `well-formed but non-existent author_id returns 400 and never calls create`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            every { author_repo_mock.exists(any(), author_id) } returns false

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"$author_id","name":"Series"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                series_repo_mock.create(any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `well-formed but non-existent universe_id returns 400 and never calls create`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            val universe_id = Uuid.random()
            every { author_repo_mock.exists(any(), author_id) } returns true
            every { universe_repo_mock.exists(any(), universe_id) } returns false

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"$author_id","universe_id":"$universe_id","name":"Series"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                series_repo_mock.create(any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `is_finished omitted returns 400 bad request`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            every { author_repo_mock.exists(any(), author_id) } returns true
            every { series_repo_mock.create(any(), author_id, null, "Series", false) } returns
                    SeriesRow(Uuid.random(), author_id, null, "Series", false)

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"$author_id","name":"Series"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)

            verify(exactly = 0)
            {
                series_repo_mock.create(any(), author_id, null, "Series", false)
            }
        }


        @Test
        fun `missing name field returns 400 not 500`() = testSeriesRoute()
        { client ->
            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id":"${Uuid.random()}"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }

        @Test
        fun `universe belonging to a different author returns 400 and never calls create`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            val universe_id = Uuid.random()
            val other_universe_record = UniverseRow(universe_id, Uuid.random(), "Name")

            every { author_repo_mock.exists(any(), author_id) } returns true
            every { universe_repo_mock.exists(any(), universe_id) } returns true
            every { universe_repo_mock.findById(any(), universe_id) } returns other_universe_record

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody(SeriesCreateRequest(author_id.toString(), universe_id.toString(), "Series", true))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { series_repo_mock.create(any(), any(), any(), any(), any()) }
        }


        @Test
        fun `universe belonging to the same author is accepted`() = testSeriesRoute()
        { client ->
            val author_id = Uuid.random()
            val universe_id = Uuid.random()
            val universe_record = UniverseRow(universe_id, author_id, "Name")

            every { author_repo_mock.exists(any(), author_id) } returns true
            every { universe_repo_mock.exists(any(), universe_id) } returns true
            every { universe_repo_mock.findById(any(), universe_id) } returns universe_record
            every { series_repo_mock.create(any(), author_id, universe_id, "Series", false) } returns
                    SeriesRow(Uuid.random(), author_id, universe_id, "Series", false)

            val response = client.post("/series")
            {
                contentType(ContentType.Application.Json)
                setBody(SeriesCreateRequest(author_id.toString(), universe_id.toString(), "Series", false))
            }

            assertEquals(HttpStatusCode.Created, response.status)
        }
    }
}
