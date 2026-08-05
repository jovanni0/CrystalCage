package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.configureStatusPages
import dev.jovanni0.data_types.BookCreateContext
import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.data_types.book.BookDetailResponse
import dev.jovanni0.data_types.book.BookSummaryResponse
import dev.jovanni0.exceptions.AuthorMismatchException
import dev.jovanni0.exceptions.not_found.AuthorNotFoundException
import dev.jovanni0.exceptions.not_found.BookNotFoundException
import dev.jovanni0.exceptions.not_found.CoverNotFoundException
import dev.jovanni0.exceptions.not_found.SeriesNotFoundException
import dev.jovanni0.exceptions.not_found.UniverseNotFoundException
import dev.jovanni0.records.book.BookDetailRow
import dev.jovanni0.records.book.BookSummaryRow
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.routes.bookRoutes
import dev.jovanni0.utils.Update
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid



class BookRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val book_repo_mock = mockk<BookRepository>()
    private val username = "test-user"
    private val schema_name = "user_$username"

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testBookRoute(block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            bookRoutes(book_repo_mock)
        }

        val client = createClient {
            install(ContentNegotiation) { json() }

            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        block(client)
    }

    fun testBookRouteNoAuth(block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            bookRoutes(book_repo_mock)
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        block(client)
    }


    @Nested
    inner class GetBooks
    {
        @Test
        fun `returns 200 with correct BookSummaryResponse array with no filters`() = testBookRoute { client ->
            val book_1 =
                BookSummaryRow(Uuid.random(), Uuid.random(), null, null, null, null, "Book A", ReadStatus.UNREAD, 0)
            val book_2 = BookSummaryRow(Uuid.random(), Uuid.random(), null, null, null, null, "Book B", ReadStatus.READ, 2)

            every { book_repo_mock.findAllBy(schema_name, null, null, null, null) } returns listOf(book_1, book_2)

            val response = client.get("/books")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<BookSummaryResponse>>()

            assertEquals(2, body.size)
            assertTrue(body.any { it.id == book_1.id.toString() && it.title == "Book A" })
            assertTrue(body.any { it.id == book_2.id.toString() && it.reread_count == 2 })
        }


        @Test
        fun `passes author_id, universe_id, series_id and tag_id query params to the repository`() = testBookRoute { client ->
            val author_id = Uuid.random()
            val universe_id = Uuid.random()
            val series_id = Uuid.random()
            val tag_id = Uuid.random()

            every { book_repo_mock.findAllBy(schema_name, author_id, universe_id, series_id, tag_id) } returns emptyList()

            val response = client.get("/books?author_id=$author_id&universe_id=$universe_id&series_id=$series_id&tag_id=$tag_id")

            assertEquals(HttpStatusCode.OK, response.status)
            verify { book_repo_mock.findAllBy(schema_name, author_id, universe_id, series_id, tag_id) }
        }


        @Test
        fun `returns 200 with empty array when no books match`() = testBookRoute { client ->
            every { book_repo_mock.findAllBy(schema_name, null, null, null, null) } returns emptyList()

            val response = client.get("/books")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.body<List<BookSummaryResponse>>().isEmpty())
        }


        @Test
        fun `returns 401 without a valid auth token`() = testBookRouteNoAuth { client ->
            val response = client.get("/books")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }


    @Nested
    inner class GetBookById
    {
        @Test
        fun `returns 200 with full BookDetailResponse for an existing book`() = testBookRoute { client ->
            val book_id = Uuid.random()
            val book = BookDetailRow(
                id = book_id,
                author_id = Uuid.random(),
                series_id = null,
                universe_id = null,
                cover_id = null,
                volume_number = null,
                title = "Detail Book",
                launch_date = null,
                word_count = null,
                blurb = null,
                notes = null,
                tag_ids = emptyList(),
                read_status = ReadStatus.UNREAD,
                reread_count = 0,
            )

            every { book_repo_mock.findById(schema_name, book_id) } returns book

            val response = client.get("/books/$book_id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<BookDetailResponse>()
            assertEquals(book_id.toString(), body.id)
            assertEquals("Detail Book", body.title)
        }


        @Test
        fun `returns 404 for a nonexistent book id`() = testBookRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.findById(schema_name, book_id) } returns null

            val response = client.get("/books/$book_id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 401 without a valid auth token`() = testBookRouteNoAuth { client ->
            val response = client.get("/books/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }


    @Nested
    inner class PostBooks
    {
        @Test
        fun `returns 200 with BookSummaryResponse when created with only required fields`() = testBookRoute { client ->
            val author_id = Uuid.random()
            val created = BookSummaryRow(Uuid.random(), author_id, null, null, null, null, "New Book", ReadStatus.UNREAD, 0)

            every { book_repo_mock.create(schema_name, any()) } returns created

            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", author_id.toString())
                            append("title", "New Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("New Book", response.body<BookSummaryResponse>().title)
        }


        @Test
        fun `parses indexed cover and edition fields and forwards them to the repository`() = testBookRoute { client ->
            val author_id = Uuid.random()
            val created = BookSummaryRow(Uuid.random(), author_id, null, null, Uuid.random(), null, "Book With Files", ReadStatus.UNREAD, 0)
            val context_slot = slot<BookCreateContext>()

            every { book_repo_mock.create(schema_name, capture(context_slot)) } returns created

            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", author_id.toString())
                            append("title", "Book With Files")
                            append("covers[0].label", "front")
                            append("covers[0].is_default", "true")
                            append(
                                "covers[0].file",
                                byteArrayOf(1, 2, 3),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"front.jpg\"")
                                }
                            )
                            append("editions[0].format", "epub")
                            append(
                                "editions[0].file",
                                byteArrayOf(4, 5, 6),
                                Headers.build {
                                    append(HttpHeaders.ContentDisposition, "filename=\"book.epub\"")
                                }
                            )
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(1, context_slot.captured.covers.size)
            assertEquals("front", context_slot.captured.covers.first().label)
            assertTrue(context_slot.captured.covers.first().is_default)
            assertEquals(1, context_slot.captured.editions.size)
            assertEquals("epub", context_slot.captured.editions.first().format)
        }


        @Test
        fun `returns 400 when author_id is missing`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("title", "No Author Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when author_id is not a valid UUID`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", "not-a-uuid")
                            append("title", "Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when title is missing`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when title is blank`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "   ")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when universe_id is not a valid UUID`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("universe_id", "not-a-uuid")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when series_id is not a valid UUID`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("series_id", "not-a-uuid")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when volume_number is not a valid number`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("volume_number", "not-a-number")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when launch_date is not a valid date`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("launch_date", "not-a-date")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when word_count is not a valid integer`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("word_count", "not-an-int")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when tag_ids contains an invalid UUID`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("tag_ids", "not-a-uuid")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when more than one cover is marked is_default`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("covers[0].is_default", "true")
                            append(
                                "covers[0].file",
                                byteArrayOf(1),
                                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"a.jpg\"") }
                            )
                            append("covers[1].is_default", "true")
                            append(
                                "covers[1].file",
                                byteArrayOf(2),
                                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"b.jpg\"") }
                            )
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when a cover entry is missing its file`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("covers[0].label", "front")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when an edition entry is missing its file`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("editions[0].format", "epub")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when an edition entry is missing its format`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append(
                                "editions[0].file",
                                byteArrayOf(1),
                                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"a.epub\"") }
                            )
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when an edition release_date is not a valid date`() = testBookRoute { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                            append("editions[0].format", "epub")
                            append("editions[0].release_date", "not-a-date")
                            append(
                                "editions[0].file",
                                byteArrayOf(1),
                                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"a.epub\"") }
                            )
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when repository throws AuthorNotFoundException`() = testBookRoute { client ->
            val author_id = Uuid.random()

            every { book_repo_mock.create(schema_name, any()) } throws AuthorNotFoundException(author_id.toString())

            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", author_id.toString())
                            append("title", "Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 400 when repository throws UniverseNotFoundException`() = testBookRoute { client ->
            every { book_repo_mock.create(schema_name, any()) } throws UniverseNotFoundException(
                Uuid.random().toString()
            )

            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 400 when repository throws SeriesNotFoundException`() = testBookRoute { client ->
            every { book_repo_mock.create(schema_name, any()) } throws SeriesNotFoundException(Uuid.random().toString())

            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 409 when repository throws AuthorMismatchException`() = testBookRoute()
        { client ->
            every { book_repo_mock.create(schema_name, any()) } throws AuthorMismatchException("author mismatch")

            val response = client.post("/books")
            {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }


        @Test
        fun `returns 401 without a valid auth token`() = testBookRouteNoAuth { client ->
            val response = client.post("/books") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("author_id", Uuid.random().toString())
                            append("title", "Book")
                        }
                    )
                )
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }


    @Nested
    inner class PatchBooks
    {
        @Test
        fun `returns 200 and updates a single field, leaving others untouched`() = testBookRoute { client ->
            val book_id = Uuid.random()
            val updated = BookDetailRow(
                id = book_id,
                author_id = Uuid.random(),
                series_id = null,
                universe_id = null,
                cover_id = null,
                volume_number = null,
                title = "Updated Title",
                launch_date = null,
                word_count = null,
                blurb = null,
                notes = null,
                tag_ids = emptyList(),
                read_status = ReadStatus.UNREAD,
                reread_count = 0,
            )

            every {
                book_repo_mock.update(
                    schema_name,
                    book_id,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Value("Updated Title"),
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                )
            } returns updated

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"title": "Updated Title"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Updated Title", response.body<BookDetailResponse>().title)
        }


        @Test
        fun `clears a nullable field when explicitly sent as null`() = testBookRoute { client ->
            val book_id = Uuid.random()
            val updated = BookDetailRow(
                id = book_id,
                author_id = Uuid.random(),
                series_id = null,
                universe_id = null,
                cover_id = null,
                volume_number = null,
                title = "Book",
                launch_date = null,
                word_count = null,
                blurb = null,
                notes = null,
                tag_ids = emptyList(),
                read_status = ReadStatus.UNREAD,
                reread_count = 0,
            )

            every {
                book_repo_mock.update(
                    schema_name,
                    book_id,
                    Update.Undefined,
                    Update.Value(null),
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                )
            } returns updated

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"universe_id": null}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
        }


        @Test
        fun `leaves a field untouched when omitted from the request body`() = testBookRoute { client ->
            val book_id = Uuid.random()
            val updated = BookDetailRow(
                id = book_id,
                author_id = Uuid.random(),
                series_id = null,
                universe_id = null,
                cover_id = null,
                volume_number = null,
                title = "Book",
                launch_date = null,
                word_count = null,
                blurb = null,
                notes = null,
                tag_ids = listOf(Uuid.random()),
                read_status = ReadStatus.UNREAD,
                reread_count = 0,
            )

            every {
                book_repo_mock.update(
                    schema_name,
                    book_id,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                    Update.Undefined,
                )
            } returns updated

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
        }


        @Test
        fun `returns 404 when repository throws BookNotFoundException`() = testBookRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.update(schema_name, book_id, any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
                    BookNotFoundException(book_id.toString())

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"title": "Whatever"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 400 when author_id is malformed`() = testBookRoute { client ->
            val book_id = Uuid.random()

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id": "not-a-uuid"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when universe_id is malformed`() = testBookRoute { client ->
            val book_id = Uuid.random()

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"universe_id": "not-a-uuid"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when series_id is malformed`() = testBookRoute { client ->
            val book_id = Uuid.random()

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"series_id": "not-a-uuid"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when cover_id is malformed`() = testBookRoute { client ->
            val book_id = Uuid.random()

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"cover_id": "not-a-uuid"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when tag_ids contains a malformed UUID`() = testBookRoute { client ->
            val book_id = Uuid.random()

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"tag_ids": ["not-a-uuid"]}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when author_id is explicitly set to null`() = testBookRoute { client ->
            val book_id = Uuid.random()

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id": null}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `returns 400 when repository throws AuthorNotFoundException`() = testBookRoute()
        { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.update(
                schema_name,
                book_id,
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            ) } throws AuthorNotFoundException(Uuid.random().toString())

            val response = client.patch("/books/$book_id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id": "${Uuid.random()}"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 404 when repository throws CoverNotFoundException`() = testBookRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.update(schema_name, book_id, any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
                    CoverNotFoundException(Uuid.random().toString())

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"cover_id": "${Uuid.random()}"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 409 when repository throws AuthorMismatchException`() = testBookRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.update(schema_name, book_id, any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws
                    AuthorMismatchException("mismatch")

            val response = client.patch("/books/$book_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"author_id": "${Uuid.random()}"}""")
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }


        @Test
        fun `returns 401 without a valid auth token`() = testBookRouteNoAuth { client ->
            val response = client.patch("/books/${Uuid.random()}") {
                contentType(ContentType.Application.Json)
                setBody("""{"title": "Whatever"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }


    @Nested
    inner class DeleteBooks
    {
        @Test
        fun `returns 204 when an existing book is deleted`() = testBookRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.delete(schema_name, book_id) } returns true

            val response = client.delete("/books/$book_id")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }


        @Test
        fun `returns 404 for a nonexistent book id`() = testBookRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.delete(schema_name, book_id) } returns false

            val response = client.delete("/books/$book_id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `returns 401 without a valid auth token`() = testBookRouteNoAuth { client ->
            val response = client.delete("/books/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }
}
