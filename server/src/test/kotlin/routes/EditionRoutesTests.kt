package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.configureStatusPages
import dev.jovanni0.data_types.edition.EditionResponse
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.records.edition.EditionRow
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.EditionRepository
import dev.jovanni0.routes.editionRoutes
import dev.jovanni0.utils.Patch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
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
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid



class EditionRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val edition_repo_mock = mockk<EditionRepository>()
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


    fun testEditionRoute(block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            editionRoutes(book_repo_mock, edition_repo_mock)
        }

        val client = createClient {
            install(ContentNegotiation) { json() }

            defaultRequest {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        block(client)
    }

    fun testEditionRouteNoAuth(block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            editionRoutes(book_repo_mock, edition_repo_mock)
        }

        val client = createClient {
            install(ContentNegotiation) { json() }
        }

        block(client)
    }


    @Nested
    inner class GetEditions
    {
        @Test
        fun `returns 200 with correct EditionResponse array for a valid book_id`() = testEditionRoute { client ->
            val book_id = Uuid.random()
            val edition_1 = EditionRow(Uuid.random(), book_id, "paperback", null, "paperback", LocalDate.now())
            val edition_2 = EditionRow(Uuid.random(), book_id, "ebook", LocalDate.of(2023, 1, 1), "ebook", LocalDate.now())

            every { edition_repo_mock.findAllByBookId(schema_name, book_id) } returns listOf(edition_1, edition_2)

            val response = client.get("/editions?book_id=$book_id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<EditionResponse>>()
            assertEquals(2, body.size)
            assertTrue(body.any { it.id == edition_1.id.toString() && it.label == "paperback" })
            assertTrue(body.any { it.id == edition_2.id.toString() && it.format == "ebook" })
        }


        @Test
        fun `returns 400 when book_id is missing`() = testEditionRoute { client ->
            val response = client.get("/editions")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.findAllByBookId(any(), any()) }
        }


        @Test
        fun `returns 400 when book_id is malformed`() = testEditionRoute { client ->
            val response = client.get("/editions?book_id=not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.findAllByBookId(any(), any()) }
        }


        @Test
        fun `returns empty array for a book with zero editions`() = testEditionRoute { client ->
            val book_id = Uuid.random()

            every { edition_repo_mock.findAllByBookId(schema_name, book_id) } returns emptyList()

            val response = client.get("/editions?book_id=$book_id")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.body<List<EditionResponse>>().isEmpty())
        }


        @Test
        fun `unauthenticated request returns 401`() = testEditionRouteNoAuth { client ->
            val response = client.get("/editions?book_id=${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { edition_repo_mock.findAllByBookId(any(), any()) }
        }
    }



    @Nested
    inner class PostEditions
    {
        private suspend fun HttpClient.postEditionMultipart(
            book_id: String? = Uuid.random().toString(),
            label: String? = null,
            release_date: String? = null,
            format: String? = "hardcover",
            file_bytes: ByteArray? = "fake-file-bytes".toByteArray(),
            file_name: String? = "edition.epub",
            field_order: List<String> = listOf("book_id", "label", "release_date", "format", "file"),
        ) = submitFormWithBinaryData(
            url = "/editions",
            formData = formData {
                val parts = mutableListOf<Pair<String, () -> Unit>>()

                if (book_id != null) parts.add("book_id" to { append("book_id", book_id) })
                if (label != null) parts.add("label" to { append("label", label) })
                if (release_date != null) parts.add("release_date" to { append("release_date", release_date) })
                if (format != null) parts.add("format" to { append("format", format) })
                if (file_bytes != null && file_name != null)
                {
                    parts.add("file" to {
                        append("file", file_bytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$file_name\"")
                        })
                    })
                }

                field_order
                    .mapNotNull { name -> parts.firstOrNull { it.first == name } }
                    .forEach { it.second() }
            }
        )


        @Test
        fun `valid multipart request with no label or release_date returns success`() = testEditionRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()
            val created_edition = EditionRow(Uuid.random(), book_id, null, null, "hardcover", LocalDate.now())

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                edition_repo_mock.create(schema_name, book_id, null, null, "hardcover", "edition.epub", any(), book_context)
            } returns created_edition

            val response = client.postEditionMultipart(book_id = book_id.toString(), label = null, release_date = null)

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<EditionResponse>()
            assertEquals(created_edition.id.toString(), body.id)
            assertNull(body.label)
            assertNull(body.release_date)
        }


        @Test
        fun `valid request including label and release_date returns them correctly`() = testEditionRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()
            val release_date = LocalDate.of(2023, 6, 15)
            val created_edition = EditionRow(Uuid.random(), book_id, "collector's edition", release_date, "hardcover", LocalDate.now())

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                edition_repo_mock.create(schema_name, book_id, "collector's edition", release_date, "hardcover", "edition.epub", any(), book_context)
            } returns created_edition

            val response = client.postEditionMultipart(
                book_id = book_id.toString(),
                label = "collector's edition",
                release_date = "2023-06-15",
            )

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<EditionResponse>()
            assertEquals("collector's edition", body.label)
            assertEquals("2023-06-15", body.release_date.toString())
        }


        @Test
        fun `missing book_id returns 400`() = testEditionRoute { client ->
            val response = client.postEditionMultipart(book_id = null)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `malformed book_id returns 400, not a 500 from an uncaught parse exception`() = testEditionRoute { client ->
            val response = client.postEditionMultipart(book_id = "not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `missing format returns 400`() = testEditionRoute { client ->
            val response = client.postEditionMultipart(format = null)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `blank format returns 400`() = testEditionRoute { client ->
            val response = client.postEditionMultipart(format = "   ")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `malformed release_date return 400`() = testEditionRoute()
        { client ->
            val response = client.postEditionMultipart(release_date = "not-a-real-date")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `missing file returns 400`() = testEditionRoute { client ->
            val response = client.postEditionMultipart(file_bytes = null, file_name = null)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `book_id valid but book does not exist returns 400`() = testEditionRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns null

            val response = client.postEditionMultipart(book_id = book_id.toString())

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `failure injection - repository throw results in no successful response`() = testEditionRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                edition_repo_mock.create(schema_name, book_id, any(), any(), any(), any(), any(), book_context)
            } throws kotlinx.io.IOException("disk full")

            val response = client.postEditionMultipart(book_id = book_id.toString())

            assertNotEquals(HttpStatusCode.OK, response.status)
        }


        @Test
        fun `multipart with the file part arriving before form fields still succeeds`() = testEditionRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()
            val created_edition = EditionRow(Uuid.random(), book_id, "front", null, "hardcover", LocalDate.now())

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                edition_repo_mock.create(schema_name, book_id, "front", null, "hardcover", "edition.epub", any(), book_context)
            } returns created_edition

            val response = client.postEditionMultipart(
                book_id = book_id.toString(),
                label = "front",
                field_order = listOf("file", "book_id", "label", "format"),
            )

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(created_edition.id.toString(), response.body<EditionResponse>().id)
        }


        @Test
        fun `unauthenticated request returns 401`() = testEditionRouteNoAuth { client ->
            val response = client.submitFormWithBinaryData(
                url = "/editions",
                formData = formData {
                    append("book_id", Uuid.random().toString())
                    append("format", "hardcover")
                    append("file", "bytes".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"edition.epub\"")
                    })
                }
            )

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { edition_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any()) }
        }
    }



    @Nested
    inner class PatchEditionById
    {
        @Test
        fun `updates one or more fields and returns 200 with updated EditionResponse`() = testEditionRoute { client ->
            val edition_id = Uuid.random()
            val updated_row = EditionRow(edition_id, Uuid.random(), "new label", null, "hardcover", LocalDate.now())

            every {
                edition_repo_mock.update(schema_name, edition_id, Patch.Value("new label"), Patch.Undefined, Patch.Undefined)
            } returns updated_row

            val response = client.patch("/editions/$edition_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("new label", response.body<EditionResponse>().label)
        }


        @Test
        fun `format null in the request body returns 400`() = testEditionRoute { client ->
            val edition_id = Uuid.random()

            val response = client.patch("/editions/$edition_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"format": null}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.update(any(), any(), any(), any(), any()) }
        }


        @Test
        fun `empty patch body with all fields undefined returns 400`() = testEditionRoute { client ->
            val edition_id = Uuid.random()

            val response = client.patch("/editions/$edition_id") {
                contentType(ContentType.Application.Json)
                setBody("""{}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.update(any(), any(), any(), any(), any()) }
        }


        @Test
        fun `label null clears the label and returns it as null`() = testEditionRoute { client ->
            val edition_id = Uuid.random()
            val updated_row = EditionRow(edition_id, Uuid.random(), null, LocalDate.of(2023, 1, 1), "hardcover", LocalDate.now())

            every {
                edition_repo_mock.update(schema_name, edition_id, Patch.Value(null), Patch.Undefined, Patch.Undefined)
            } returns updated_row

            val response = client.patch("/editions/$edition_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": null}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertNull(response.body<EditionResponse>().label)
        }


        @Test
        fun `release_date null clears the release date`() = testEditionRoute { client ->
            val edition_id = Uuid.random()
            val updated_row = EditionRow(edition_id, Uuid.random(), "label", null, "hardcover", LocalDate.now())

            every {
                edition_repo_mock.update(schema_name, edition_id, Patch.Undefined, Patch.Value(null), Patch.Undefined)
            } returns updated_row

            val response = client.patch("/editions/$edition_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"release_date": null}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertNull(response.body<EditionResponse>().release_date)
        }


        @Test
        fun `updating a nonexistent edition id returns 404`() = testEditionRoute { client ->
            val edition_id = Uuid.random()

            every {
                edition_repo_mock.update(schema_name, edition_id, Patch.Value("new label"), Patch.Undefined, Patch.Undefined)
            } returns null

            val response = client.patch("/editions/$edition_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id path param returns 400`() = testEditionRoute { client ->
            val response = client.patch("/editions/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.update(any(), any(), any(), any(), any()) }
        }


        @Test
        fun `unauthenticated request returns 401`() = testEditionRouteNoAuth { client ->
            val response = client.patch("/editions/${Uuid.random()}") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { edition_repo_mock.update(any(), any(), any(), any(), any()) }
        }
    }



    @Nested
    inner class DeleteEditionById
    {
        @Test
        fun `deleting an existing edition returns 204`() = testEditionRoute { client ->
            val edition_id = Uuid.random()

            every { edition_repo_mock.delete(schema_name, edition_id) } returns true

            val response = client.delete("/editions/$edition_id")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }


        @Test
        fun `deleting a nonexistent edition returns 404`() = testEditionRoute { client ->
            val edition_id = Uuid.random()

            every { edition_repo_mock.delete(schema_name, edition_id) } returns false

            val response = client.delete("/editions/$edition_id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id path param returns 400`() = testEditionRoute { client ->
            val response = client.delete("/editions/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { edition_repo_mock.delete(any(), any()) }
        }


        @Test
        fun `unauthenticated request returns 401`() = testEditionRouteNoAuth { client ->
            val response = client.delete("/editions/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { edition_repo_mock.delete(any(), any()) }
        }
    }
}
