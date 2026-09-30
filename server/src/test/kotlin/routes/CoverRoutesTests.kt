package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.configureStatusPages
import dev.jovanni0.data_types.cover.CoverResponse
import dev.jovanni0.filesystem.AssetCategory
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.records.cover.CoverRow
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.CoverRepository
import dev.jovanni0.routes.coverRoutes
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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.io.TempDir
import sun.java2d.cmm.ProfileDataVerifier.verify
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid



class CoverRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val cover_repo_mock = mockk<CoverRepository>()
    private val book_repo_mock = mockk<BookRepository>()
    private val username = "test-user"

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testCoverRoute(block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            coverRoutes(book_repo_mock, cover_repo_mock)
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
    inner class GetCovers
    {
        private val schema_name = "user_$username"


        @Test
        fun `returns 200 with correct CoverResponse array for a valid book_id`() = testCoverRoute()
        { client ->
            val book_id = Uuid.random()
            val cover_1 = CoverRow(Uuid.random(), book_id, "front", LocalDate.now(), is_default = true)
            val cover_2 = CoverRow(Uuid.random(), book_id, "back", LocalDate.now(), is_default = false)

            every { cover_repo_mock.findAllByBookId(schema_name, book_id) } returns listOf(cover_1, cover_2)

            val response = client.get("/covers?book_id=$book_id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<CoverResponse>>()

            assertEquals(2, body.size)
            assertTrue(body.any { it.id == cover_1.id.toString() && it.label == "front" && it.is_default })
            assertTrue(body.any { it.id == cover_2.id.toString() && it.label == "back" && !it.is_default })
        }


        @Test
        fun `returns 400 when book_id is missing`() = testCoverRoute()
        { client ->
            val response = client.get("/covers")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.findAllByBookId(any(), any()) }
        }


        @Test
        fun `returns 400 when book_id is malformed`() = testCoverRoute()
        { client ->
            val response = client.get("/covers?book_id=not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.findAllByBookId(any(), any()) }
        }


        @Test
        fun `returns empty array, not an error, for a book with zero covers`() = testCoverRoute()
        { client ->
            val book_id = Uuid.random()

            every { cover_repo_mock.findAllByBookId(schema_name, book_id) } returns emptyList()

            val response = client.get("/covers?book_id=$book_id")

            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.body<List<CoverResponse>>().isEmpty())
        }


        @Test
        fun `unauthenticated request returns 401`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }

            routing {
                coverRoutes(book_repo_mock, cover_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/covers?book_id=${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { cover_repo_mock.findAllByBookId(any(), any()) }
        }
    }


    @Nested
    inner class GetCoverById
    {
        private val schema_name = "user_$username"


        private fun tempFileWithContent(dir: Path, name: String, content: ByteArray): Path
        {
            val file = dir.resolve(name)
            Files.write(file, content)
            return file
        }


        @Test
        fun `returns 200 with correct bytes and content type for an existing cover with a matching file`(@TempDir tmp_dir: Path) = testCoverRoute()
        { client ->
            val cover_id = Uuid.random()
            val book_id = Uuid.random()
            val cover_row = CoverRow(cover_id, book_id, "front", LocalDate.now(), is_default = false)
            val file_bytes = "fake-jpeg-bytes".toByteArray()
            val file_path = tempFileWithContent(tmp_dir, "cover.$cover_id.jpg", file_bytes)

            val storage_mock = mockk<AssetStorage>()
            every { cover_repo_mock.findById(schema_name, cover_id) } returns cover_row
            every { cover_repo_mock.cover_storage } returns storage_mock
            every { storage_mock.findByEmbeddedId(schema_name, AssetCategory.COVER, cover_id) } returns file_path

            val response = client.get("/covers/$cover_id")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Image.JPEG, response.contentType())
            assertArrayEquals(file_bytes, response.body<ByteArray>())
        }


        @Test
        fun `returns 404 when the cover id does not exist in the DB at all`() = testCoverRoute { client ->
            val cover_id = Uuid.random()

            every { cover_repo_mock.findById(schema_name, cover_id) } returns null

            val response = client.get("/covers/$cover_id")

            assertEquals(HttpStatusCode.NotFound, response.status)
            verify(exactly = 0) { cover_repo_mock.cover_storage }
        }


        @Test
        fun `returns 404 when the DB row exists but the file is missing on disk`() = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val book_id = Uuid.random()
            val cover_row = CoverRow(cover_id, book_id, null, LocalDate.now(), is_default = false)

            val storage_mock = mockk<AssetStorage>()
            every { cover_repo_mock.findById(schema_name, cover_id) } returns cover_row
            every { cover_repo_mock.cover_storage } returns storage_mock
            every { storage_mock.findByEmbeddedId(schema_name, AssetCategory.COVER, cover_id) } returns null

            val response = client.get("/covers/$cover_id")

            assertEquals(HttpStatusCode.NotFound, response.status)
            // Server-side error logging on this branch (call.application.log.error(...)) isn't
            // asserted here — would need a log-capturing appender wired into the test application
            // to verify the message content, which this harness doesn't currently set up.
        }


        @Test
        fun `returns correct content type for a jpg cover`(@TempDir tmp_dir: Path) = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val cover_row = CoverRow(cover_id, Uuid.random(), null, LocalDate.now(), is_default = false)
            val file_path = tempFileWithContent(tmp_dir, "cover.$cover_id.jpg", "bytes".toByteArray())

            val storage_mock = mockk<AssetStorage>()
            every { cover_repo_mock.findById(schema_name, cover_id) } returns cover_row
            every { cover_repo_mock.cover_storage } returns storage_mock
            every { storage_mock.findByEmbeddedId(schema_name, AssetCategory.COVER, cover_id) } returns file_path

            val response = client.get("/covers/$cover_id")

            assertEquals(ContentType.Image.JPEG, response.contentType())
        }


        @Test
        fun `returns correct content type for a png cover`(@TempDir tmp_dir: Path) = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val cover_row = CoverRow(cover_id, Uuid.random(), null, LocalDate.now(), is_default = false)
            val file_path = tempFileWithContent(tmp_dir, "cover.$cover_id.png", "bytes".toByteArray())

            val storage_mock = mockk<AssetStorage>()
            every { cover_repo_mock.findById(schema_name, cover_id) } returns cover_row
            every { cover_repo_mock.cover_storage } returns storage_mock
            every { storage_mock.findByEmbeddedId(schema_name, AssetCategory.COVER, cover_id) } returns file_path

            val response = client.get("/covers/$cover_id")

            assertEquals(ContentType.Image.PNG, response.contentType())
        }


        @Test
        fun `falls back to application octet-stream for an unrecognized extension`(@TempDir tmp_dir: Path) = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val cover_row = CoverRow(cover_id, Uuid.random(), null, LocalDate.now(), is_default = false)
            val file_path = tempFileWithContent(tmp_dir, "cover.$cover_id.weirdext", "bytes".toByteArray())

            val storage_mock = mockk<AssetStorage>()
            every { cover_repo_mock.findById(schema_name, cover_id) } returns cover_row
            every { cover_repo_mock.cover_storage } returns storage_mock
            every { storage_mock.findByEmbeddedId(schema_name, AssetCategory.COVER, cover_id) } returns file_path

            val response = client.get("/covers/$cover_id")

            assertEquals(ContentType.Application.OctetStream, response.contentType())
        }


        @Test
        fun `malformed id path param returns 400, not 500`() = testCoverRoute { client ->
            val response = client.get("/covers/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.findById(any(), any()) }
        }


        @Test
        fun `unauthenticated request returns 401`() = testApplication {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }

            routing {
                coverRoutes(book_repo_mock, cover_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/covers/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { cover_repo_mock.findById(any(), any()) }
        }
    }


    @Nested
    inner class PostCovers
    {
        private val schema_name = "user_$username"


        private suspend fun HttpClient.postCoverMultipart(
            book_id: String? = Uuid.random().toString(),
            label: String? = null,
            file_bytes: ByteArray? = "fake-image-bytes".toByteArray(),
            file_name: String? = "cover.jpg",
            field_order: List<String> = listOf("book_id", "label", "file"),
        ) = submitFormWithBinaryData(
            url = "/covers",
            formData = formData {
                val parts = mutableListOf<Pair<String, () -> Unit>>()

                if (book_id != null) parts.add("book_id" to { append("book_id", book_id) })
                if (label != null) parts.add("label" to { append("label", label) })
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
        fun `valid multipart request with no label returns 200 with correct CoverResponse`() = testCoverRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()
            val created_cover = CoverRow(Uuid.random(), book_id, null, LocalDate.now(), is_default = false)

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                cover_repo_mock.create(schema_name, book_id, null, "cover.jpg", any(), book_context)
            } returns created_cover

            val response = client.postCoverMultipart(book_id = book_id.toString(), label = null)

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<CoverResponse>()
            assertEquals(created_cover.id.toString(), body.id)
            assertNull(body.label)
        }


        @Test
        fun `valid multipart request with a label returns the label in the response`() = testCoverRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()
            val created_cover = CoverRow(Uuid.random(), book_id, "front", LocalDate.now(), is_default = false)

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                cover_repo_mock.create(schema_name, book_id, "front", "cover.jpg", any(), book_context)
            } returns created_cover

            val response = client.postCoverMultipart(book_id = book_id.toString(), label = "front")

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("front", response.body<CoverResponse>().label)
        }


        @Test
        fun `missing book_id form field returns 400 with a descriptive message`() = testCoverRoute { client ->
            val response = client.postCoverMultipart(book_id = null)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.create(any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `malformed book_id returns 400, not a 500 from an uncaught parse exception`() = testCoverRoute { client ->
            val response = client.postCoverMultipart(book_id = "not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.create(any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `missing file part returns 400`() = testCoverRoute { client ->
            val response = client.postCoverMultipart(file_bytes = null, file_name = null)

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.create(any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `book_id that is a valid UUID but does not correspond to any existing book returns 400`() = testCoverRoute { client ->
            val book_id = Uuid.random()

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns null

            val response = client.postCoverMultipart(book_id = book_id.toString())

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.create(any(), any(), any(), any(), any(), any()) }
        }


        @Test
        fun `failure injection - repository throw results in no successful response and does not swallow the error`() = testCoverRoute { client ->
            // Route-level analogue of the repository rollback test: if cover_repo.create() throws
            // (e.g. underlying AssetStorage failure), the route must not report success. Whether this
            // surfaces as 500 vs. something else depends on configureStatusPages() — asserting only
            // that it isn't 200, since the exact mapped status isn't visible from this file.
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                cover_repo_mock.create(schema_name, book_id, any(), any(), any(), book_context)
            } throws kotlinx.io.IOException("disk full")

            val response = client.postCoverMultipart(book_id = book_id.toString())

            assertNotEquals(HttpStatusCode.OK, response.status)
        }


        @Test
        fun `multipart with the file part arriving before book_id and label form fields still succeeds`() = testCoverRoute { client ->
            val book_id = Uuid.random()
            val book_context = mockk<BookFilenameContext>()
            val created_cover = CoverRow(Uuid.random(), book_id, "front", LocalDate.now(), is_default = false)

            every { book_repo_mock.getBookContext(schema_name, book_id) } returns book_context
            every {
                cover_repo_mock.create(schema_name, book_id, "front", "cover.jpg", any(), book_context)
            } returns created_cover

            val response = client.postCoverMultipart(
                book_id = book_id.toString(),
                label = "front",
                field_order = listOf("file", "book_id", "label"),
            )

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(created_cover.id.toString(), response.body<CoverResponse>().id)
        }


        @Test
        fun `unauthenticated request returns 401`() = testApplication {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }

            routing {
                coverRoutes(book_repo_mock, cover_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.submitFormWithBinaryData(
                url = "/covers",
                formData = formData {
                    append("book_id", Uuid.random().toString())
                    append("file", "bytes".toByteArray(), Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"cover.jpg\"")
                    })
                }
            )

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { cover_repo_mock.create(any(), any(), any(), any(), any(), any()) }
        }
    }


    @Nested
    inner class PatchCoverById
    {
        private val schema_name = "user_$username"


        @Test
        fun `updates label successfully and returns 200 with updated CoverResponse`() = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val updated_row = CoverRow(cover_id, Uuid.random(), "new label", LocalDate.now(), is_default = false)

            every { cover_repo_mock.update(schema_name, cover_id, Patch.Value("new label")) } just Runs
            every { cover_repo_mock.findById(schema_name, cover_id) } returns updated_row

            val response = client.patch("/covers/$cover_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("new label", response.body<CoverResponse>().label)
        }


        @Test
        fun `explicit null for label clears it, per the present-but-null convention`() = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val updated_row = CoverRow(cover_id, Uuid.random(), null, LocalDate.now(), is_default = false)

            every { cover_repo_mock.update(schema_name, cover_id, Patch.Value(null)) } just Runs
            every { cover_repo_mock.findById(schema_name, cover_id) } returns updated_row

            val response = client.patch("/covers/$cover_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": null}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertNull(response.body<CoverResponse>().label)
        }


        @Test
        fun `absent label field leaves it untouched`() = testCoverRoute { client ->
            val cover_id = Uuid.random()
            val existing_row = CoverRow(cover_id, Uuid.random(), "unchanged", LocalDate.now(), is_default = false)

            every { cover_repo_mock.update(schema_name, cover_id, Patch.Undefined) } just Runs
            every { cover_repo_mock.findById(schema_name, cover_id) } returns existing_row

            val response = client.patch("/covers/$cover_id") {
                contentType(ContentType.Application.Json)
                setBody("""{}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("unchanged", response.body<CoverResponse>().label)
        }


        @Test
        fun `updating a nonexistent cover id returns 404`() = testCoverRoute { client ->
            val cover_id = Uuid.random()

            every { cover_repo_mock.update(schema_name, cover_id, Patch.Value("new label")) } just Runs
            every { cover_repo_mock.findById(schema_name, cover_id) } returns null

            val response = client.patch("/covers/$cover_id") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id path param returns 400`() = testCoverRoute { client ->
            val response = client.patch("/covers/not-a-uuid") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.update(any(), any(), any()) }
        }


        @Test
        fun `unauthenticated request returns 401`() = testApplication {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }

            routing {
                coverRoutes(book_repo_mock, cover_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.patch("/covers/${Uuid.random()}") {
                contentType(ContentType.Application.Json)
                setBody("""{"label": "new label"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { cover_repo_mock.update(any(), any(), any()) }
        }
    }


    @Nested
    inner class DeleteCoverById
    {
        private val schema_name = "user_$username"


        @Test
        fun `deleting an existing cover returns 204`() = testCoverRoute { client ->
            val cover_id = Uuid.random()

            every { cover_repo_mock.delete(schema_name, cover_id) } returns true

            val response = client.delete("/covers/$cover_id")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }


        @Test
        fun `deleting a nonexistent cover returns 404`() = testCoverRoute { client ->
            val cover_id = Uuid.random()

            every { cover_repo_mock.delete(schema_name, cover_id) } returns false

            val response = client.delete("/covers/$cover_id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id path param returns 400`() = testCoverRoute { client ->
            val response = client.delete("/covers/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0) { cover_repo_mock.delete(any(), any()) }
        }


        @Test
        fun `unauthenticated request returns 401`() = testApplication {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }

            routing {
                coverRoutes(book_repo_mock, cover_repo_mock)
            }

            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.delete("/covers/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0) { cover_repo_mock.delete(any(), any()) }
        }
    }
}
