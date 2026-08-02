package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.data_types.tags.TagDetailResponse
import dev.jovanni0.data_types.tags.TagResponse
import dev.jovanni0.records.tags.TagBookRow
import dev.jovanni0.records.tags.TagCountRow
import dev.jovanni0.records.tags.TagDetailRow
import dev.jovanni0.repositories.TagRepository
import dev.jovanni0.routes.tagRoutes
import dev.jovanni0.utils.Patch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid



class TagRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val tag_repo_mock = mockk<TagRepository>()
    private val username = "test-user"

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testTagRoute(repo: TagRepository, block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
        }

        routing {
            tagRoutes(repo)
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
    inner class GetTags
    {
        @Test
        fun `returns 200 with mapped list matching repo output`() = testTagRoute(tag_repo_mock)
        { client ->
            val id1 = Uuid.random()
            val id2 = Uuid.random()
            every {
                tag_repo_mock.findAll(any())
            } returns listOf(
                TagCountRow(id1, "Fantasy", "Books with magic", 3),
                TagCountRow(id2, "Sci-Fi", null, 0),
            )

            val response = client.get("/tags")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<TagResponse>>()

            assertEquals(2, body.size)
            assertEquals(id1.toString(), body[0].id)
            assertEquals("Fantasy", body[0].name)
            assertEquals("Books with magic", body[0].description)
            assertEquals(3, body[0].book_count)
            assertEquals(id2.toString(), body[1].id)
            assertNull(body[1].description)
            assertEquals(0, body[1].book_count)
        }


        @Test
        fun `empty repo result returns 200 with empty array`() = testTagRoute(tag_repo_mock)
        { client ->
            every { tag_repo_mock.findAll(any()) } returns emptyList()

            val response = client.get("/tags")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<TagResponse>>()

            assertEquals(0, body.size)
        }


        @Test
        fun `calls repo exactly once with the resolved tenant schema`() = testTagRoute(tag_repo_mock)
        { client ->
            every { tag_repo_mock.findAll(any()) } returns emptyList()

            client.get("/tags")

            verify(exactly = 1)
            {
                tag_repo_mock.findAll(any())
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
                tagRoutes(tag_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/tags")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                tag_repo_mock.findAll(any())
            }
        }
    }


    @Nested
    inner class GetTagById
    {
        @Test
        fun `existing id returns 200 with correctly serialized body`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            val book_id = Uuid.random()
            val series_id = Uuid.random()
            every {
                tag_repo_mock.findById(any(), id)
            } returns TagDetailRow(
                id = id,
                name = "Fantasy",
                description = "Books with magic",
                books = listOf(TagBookRow(book_id, "Some Title", series_id, null))
            )

            val response = client.get("/tags/$id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<TagDetailResponse>()

            assertEquals(id.toString(), body.id)
            assertEquals("Fantasy", body.name)
            assertEquals(1, body.books.size)
            assertEquals(book_id.toString(), body.books.first().id)
            assertEquals("Some Title", body.books.first().title)
            assertEquals(series_id.toString(), body.books.first().series_id)
            assertNull(body.books.first().cover_id)
        }


        @Test
        fun `tag with no books returns 200 with empty books array`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.findById(any(), id) } returns TagDetailRow(id, "Fantasy", null, emptyList())

            val response = client.get("/tags/$id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<TagDetailResponse>()

            assertEquals(0, body.books.size)
        }


        @Test
        fun `non-existent id returns 404`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.findById(any(), id) } returns null

            val response = client.get("/tags/$id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id returns 400 and never calls repo`() = testTagRoute(tag_repo_mock)
        { client ->
            val response = client.get("/tags/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                tag_repo_mock.findById(any(), any())
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
                tagRoutes(tag_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/tags/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                tag_repo_mock.findById(any(), any())
            }
        }
    }


    @Nested
    inner class PatchTag
    {
        @Test
        fun `updates name only, repo called with Value for name and Undefined for description`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.update(any(), id, Patch.Value("New Name"), Patch.Undefined, emptyList(), emptyList()) } just Runs
            every { tag_repo_mock.findById(any(), id) } returns TagDetailRow(id, "New Name", null, emptyList())

            val response = client.patch("/tags/$id", fun HttpRequestBuilder.()
            {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"New Name"}""")
            })

            assertEquals(HttpStatusCode.OK, response.status)
            verify { tag_repo_mock.update(any(), id, Patch.Value("New Name"), Patch.Undefined, emptyList(), emptyList()) }
        }


        @Test
        fun `sets description to null via explicit null in JSON`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Value(null), emptyList(), emptyList()) } just Runs
            every { tag_repo_mock.findById(any(), id) } returns TagDetailRow(id, "Fantasy", null, emptyList())

            val response = client.patch("/tags/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"description":null}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            verify { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Value(null), emptyList(), emptyList()) }
        }


        @Test
        fun `omitted name and description are passed as Undefined`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, emptyList(), emptyList()) } just Runs
            every { tag_repo_mock.findById(any(), id) } returns TagDetailRow(id, "Fantasy", null, emptyList())

            val response = client.patch("/tags/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            verify { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, emptyList(), emptyList()) }
        }


        @Test
        fun `adds books via added_book_ids, correctly parsed and passed to repo`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            val book_id = Uuid.random()
            every { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, listOf(book_id), emptyList()) } just Runs
            every { tag_repo_mock.findById(any(), id) } returns TagDetailRow(id, "Fantasy", null, listOf(TagBookRow(book_id, "Some Book", null, null)))

            val response = client.patch("/tags/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"added_book_ids":["$book_id"]}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            verify { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, listOf(book_id), emptyList()) }
        }


        @Test
        fun `removes books via removed_book_ids, correctly parsed and passed to repo`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            val book_id = Uuid.random()
            every { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, emptyList(), listOf(book_id)) } just Runs
            every { tag_repo_mock.findById(any(), id) } returns TagDetailRow(id, "Fantasy", null, emptyList())

            val response = client.patch("/tags/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"removed_book_ids":["$book_id"]}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            verify { tag_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, emptyList(), listOf(book_id)) }
        }


        @Test
        fun `malformed tag id returns 400 and never calls repo`() = testTagRoute(tag_repo_mock)
        { client ->
            val response = client.patch("/tags/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"New Name"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { tag_repo_mock.update(any(), any(), any(), any(), any(), any()) }
            verify(exactly = 0) { tag_repo_mock.findById(any(), any()) }
        }


        @Test
        fun `invalid uuid in added_book_ids returns 400 with field name in message, never calls update`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()

            val response = client.patch("/tags/$id") {
                contentType(ContentType.Application.Json)
                setBody("""{"added_book_ids":["not-a-uuid"]}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue(response.bodyAsText().contains("added_book_ids"))
            verify(exactly = 0)
            {
                tag_repo_mock.update(any(), any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `invalid uuid in removed_book_ids returns 400 with field name in message, never calls update`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()

            val response = client.patch("/tags/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"removed_book_ids":["not-a-uuid"]}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue(response.bodyAsText().contains("removed_book_ids"))
            verify(exactly = 0)
            {
                tag_repo_mock.update(any(), any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `invalid uuid in added_book_ids short-circuits before removed_book_ids is checked`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()

            val response = client.patch("/tags/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"added_book_ids":["not-a-uuid"],"removed_book_ids":["also-not-a-uuid"]}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue(response.bodyAsText().contains("added_book_ids"), "added_book_ids is parsed first, so its error should win")
        }


        @Test
        fun `tag not found after update returns 404 with message`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.update(any(), id, Patch.Value("New Name"), Patch.Undefined, emptyList(), emptyList()) } just Runs
            every { tag_repo_mock.findById(any(), id) } returns null

            val response = client.patch("/tags/$id") {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"New Name"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertTrue(response.bodyAsText().contains(id.toString()))
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
            }
            routing {
                tagRoutes(tag_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.patch("/tags/${Uuid.random()}")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"name":"New Name"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                tag_repo_mock.update(any(), any(), any(), any(), any(), any())
            }
        }
    }


    @Nested
    inner class DeleteTag
    {
        @Test
        fun `existing id returns 204 and calls repo once`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.delete(any(), id) } returns 1

            val response = client.delete("/tags/$id")

            assertEquals(HttpStatusCode.NoContent, response.status)
            verify(exactly = 1)
            {
                tag_repo_mock.delete(any(), id)
            }
        }


        @Test
        fun `non-existent id returns 404`() = testTagRoute(tag_repo_mock)
        { client ->
            val id = Uuid.random()
            every { tag_repo_mock.delete(any(), id) } returns 0

            val response = client.delete("/tags/$id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id returns 400 and never calls repo`() = testTagRoute(tag_repo_mock)
        { client ->
            val response = client.delete("/tags/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                tag_repo_mock.delete(any(), any())
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
                tagRoutes(tag_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.delete("/tags/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { tag_repo_mock.delete(any(), any()) }
        }
    }
}
