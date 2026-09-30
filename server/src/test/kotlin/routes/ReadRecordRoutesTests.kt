package routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.jovanni0.auth.AuthConfig
import dev.jovanni0.configureAuth
import dev.jovanni0.configureSerializer
import dev.jovanni0.configureStatusPages
import dev.jovanni0.data_types.read_record.ReadRecordResponse
import dev.jovanni0.exceptions.ConfigurationException
import dev.jovanni0.exceptions.ConflictException
import dev.jovanni0.records.read_record.ReadRecordRow
import dev.jovanni0.repositories.ReadRecordRepository
import dev.jovanni0.routes.readRecordRoutes
import dev.jovanni0.utils.Patch
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
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
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.uuid.Uuid



class ReadRecordRoutesTests
{
    private val jwt_config = AuthConfig(
        secret = "test-secret",
        issuer = "test-issuer",
        audience = "test-audience",
        realm = "test-realm"
    )

    private val read_record_repo_mock = mockk<ReadRecordRepository>()
    private val username = "test-user"

    private val token = JWT
        .create()
        .withIssuer(jwt_config.issuer)
        .withAudience(jwt_config.audience)
        .withClaim("realm", jwt_config.realm)
        .withClaim("user_id", username)
        .sign(Algorithm.HMAC256(jwt_config.secret))


    fun testReadRecordRoute(repo: ReadRecordRepository, block: suspend (HttpClient) -> Unit) = testApplication()
    {
        application {
            configureSerializer()
            configureAuth(jwt_config)
            configureStatusPages()
        }

        routing {
            readRecordRoutes(repo)
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
    inner class GetReadRecords
    {
        @Test
        fun `valid book_id returns 200 with mapped list`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val book_id = Uuid.random()
            val record_id = Uuid.random()
            every { read_record_repo_mock.findAllByBookId(any(), book_id) } returns listOf(
                ReadRecordRow(record_id, book_id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)
            )

            val response = client.get("/read-records?book_id=$book_id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<List<ReadRecordResponse>>()

            assertEquals(1, body.size)
            assertEquals(record_id.toString(), body.first().id)
        }


        @Test
        fun `missing book_id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.get("/read-records")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.findAllByBookId(any(), any())
            }
        }


        @Test
        fun `malformed book_id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.get("/read-records?book_id=not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.findAllByBookId(any(), any())
            }
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }
            routing {
                readRecordRoutes(read_record_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/read-records?book_id=${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.findAllByBookId(any(), any())
            }
        }
    }


    @Nested
    inner class GetReadRecordById
    {
        @Test
        fun `existing id returns 200 with body`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            val book_id = Uuid.random()
            every { read_record_repo_mock.findById(any(), id) } returns
                    ReadRecordRow(id, book_id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val response = client.get("/read-records/$id")

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<ReadRecordResponse>()

            assertEquals(id.toString(), body.id)
        }


        @Test
        fun `non-existent id returns 404`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            every { read_record_repo_mock.findById(any(), id) } returns null

            val response = client.get("/read-records/$id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.get("/read-records/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.findById(any(), any())
            }
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }
            routing {
                readRecordRoutes(read_record_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.get("/read-records/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.findById(any(), any())
            }
        }
    }


    @Nested
    inner class PostReadRecord
    {
        @Test
        fun `valid full payload returns 201 with body`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val book_id = Uuid.random()
            val created_id = Uuid.random()
            every {
                read_record_repo_mock.create(any(), book_id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "pos", "note")
            } returns ReadRecordRow(created_id, book_id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "pos", "note")

            val response = client.post("/read-records")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"book_id":"$book_id","start_date":"2024-01-01","end_date":"2024-01-10","did_not_finish":false,"is_historical":false,"stop_position":"pos","notes":"note"}""")
            }

            assertEquals(HttpStatusCode.Created, response.status)

            val body = response.body<ReadRecordResponse>()

            assertEquals(created_id.toString(), body.id)
        }


        @Test
        fun `valid payload without edition_id succeeds with null edition_id`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val book_id = Uuid.random()
            val created_id = Uuid.random()
            every {
                read_record_repo_mock.create(any(), book_id, null, LocalDate.of(2024, 1, 1), null, false, false, null, null)
            } returns ReadRecordRow(created_id, book_id, null, LocalDate.of(2024, 1, 1), null, false, false, null, null)

            val response = client.post("/read-records")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"book_id":"$book_id","start_date":"2024-01-01","did_not_finish":false,"is_historical":false}""")
            }

            assertEquals(HttpStatusCode.Created, response.status)

            val body = response.body<ReadRecordResponse>()

            assertNull(body.edition_id)
        }


        @Test
        fun `malformed book_id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.post("/read-records") {
                contentType(ContentType.Application.Json)
                setBody("""{"book_id":"not-a-uuid","start_date":"2024-01-01","did_not_finish":false,"is_historical":false}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `malformed edition_id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.post("/read-records")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"book_id":"${Uuid.random()}","edition_id":"not-a-uuid","start_date":"2024-01-01","did_not_finish":false,"is_historical":false}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `repo throwing ConflictException on duplicate historical read returns 409`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val book_id = Uuid.random()
            every {
                read_record_repo_mock.create(any(), book_id, null, null, null, false, true, null, null)
            } throws ConflictException("book $book_id already has a historical read")

            val response = client.post("/read-records")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"book_id":"$book_id","did_not_finish":false,"is_historical":true}""")
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }
            routing {
                readRecordRoutes(read_record_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.post("/read-records")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"book_id":"${Uuid.random()}","start_date":"2024-01-01","did_not_finish":false,"is_historical":false}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.create(any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }
    }


    @Nested
    inner class PatchReadRecord
    {
        @Test
        fun `valid single-field update returns 200 with fresh body`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            val book_id = Uuid.random()
            every {
                read_record_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value("new pos"), Patch.Undefined)
            } returns 1
            every { read_record_repo_mock.findById(any(), id) } returns
                    ReadRecordRow(id, book_id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, "new pos", null)

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"stop_position":"new pos"}""")
            }

            assertEquals(HttpStatusCode.OK, response.status)

            val body = response.body<ReadRecordResponse>()

            assertEquals("new pos", body.stop_position)
        }


        @Test
        fun `malformed id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.patch("/read-records/not-a-uuid")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"stop_position":"new pos"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.update(any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `malformed edition_id in body returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"edition_id":"not-a-uuid"}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.update(any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }


        @Test
        fun `update on non-existent id returns 404`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            every {
                read_record_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value("new pos"), Patch.Undefined)
            } returns 0

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"stop_position":"new pos"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `empty body on existing record returns 400 with unchanged record`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            val book_id = Uuid.random()
            every {
                read_record_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined)
            } returns null
            every { read_record_repo_mock.findById(any(), id) } returns
                    ReadRecordRow(id, book_id, null, LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 10), false, false, null, null)

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `findById returning null right after a successful update returns 404`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            every {
                read_record_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value("new pos"), Patch.Undefined)
            } returns 1
            every { read_record_repo_mock.findById(any(), id) } returns null

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"stop_position":"new pos"}""")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `repo throwing ConflictException on invalid historical transition returns 409`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
//            every {
//                read_record_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value(true), Patch.Undefined, Patch.Undefined)
//            } throws ConflictException("a historical record cannot have start_date or end_date set")
            every {
                read_record_repo_mock.update(any(), eq(id), any(), any(), any(), any(), any(), any(), any())
            } throws ConflictException("a historical record cannot have start_date or end_date set")

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"is_historical":true}""")
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
        }


        @Test
        fun `repo throwing ConfigurationException on null is_historical returns 400`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            every {
                read_record_repo_mock.update(any(), id, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Undefined, Patch.Value(null), Patch.Undefined, Patch.Undefined)
            } throws ConfigurationException("is_historical cannot be null")

            val response = client.patch("/read-records/$id")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"is_historical":null}""")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication()
        {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }
            routing {
                readRecordRoutes(read_record_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.patch("/read-records/${Uuid.random()}")
            {
                contentType(ContentType.Application.Json)
                setBody("""{"stop_position":"new pos"}""")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.update(any(), any(), any(), any(), any(), any(), any(), any(), any())
            }
        }
    }


    @Nested
    inner class DeleteReadRecord
    {
        @Test
        fun `existing id returns 204`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            every { read_record_repo_mock.delete(any(), id) } returns 1

            val response = client.delete("/read-records/$id")

            assertEquals(HttpStatusCode.NoContent, response.status)
        }


        @Test
        fun `non-existent id returns 404`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val id = Uuid.random()
            every { read_record_repo_mock.delete(any(), id) } returns 0

            val response = client.delete("/read-records/$id")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }


        @Test
        fun `malformed id returns 400 and never calls repo`() = testReadRecordRoute(read_record_repo_mock)
        { client ->
            val response = client.delete("/read-records/not-a-uuid")

            assertEquals(HttpStatusCode.BadRequest, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.delete(any(), any())
            }
        }


        @Test
        fun `missing token returns 401 and never calls repo`() = testApplication {
            application {
                configureSerializer()
                configureAuth(jwt_config)
                configureStatusPages()
            }
            routing {
                readRecordRoutes(read_record_repo_mock)
            }
            val client = createClient {
                install(ContentNegotiation) { json() }
            }

            val response = client.delete("/read-records/${Uuid.random()}")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            verify(exactly = 0)
            {
                read_record_repo_mock.delete(any(), any())
            }
        }
    }
}
