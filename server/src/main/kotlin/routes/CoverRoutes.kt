package dev.jovanni0.routes

import dev.jovanni0.data_types.cover.CoverUpdateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.filesystem.AssetCategory
import dev.jovanni0.filesystem.AssetStorage
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.CoverRepository
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.requiredUuidQueryParam
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.forEachPart
import io.ktor.http.content.PartData
import io.ktor.server.application.log
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.utils.io.toByteArray
import java.nio.file.Files



fun Route.coverRoutes(
    book_repository: BookRepository,
    cover_repository: CoverRepository
)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        get("/covers")
        {
            val schema_name = call.tenant_schema
            val book_id = call.requiredUuidQueryParam("book_id") ?: return@get

            val result = cover_repository
                .findAllByBookId(schema_name, book_id)
                .map { it.toCoverResponse() }

            call.respond(result)
        }


        get("/covers/{id}")
        {
            val schema_name = call.tenant_schema
            val cover_id = call.getUuidCallParamOrRespond("id") ?: return@get

            val cover = cover_repository.findById(schema_name, cover_id)

            if (cover == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val file_path = cover_repository.cover_storage.findByEmbeddedId(schema_name, AssetCategory.COVER, cover_id)

            if (file_path == null)
            {
                call.application.log.error("cover $cover_id exists in DB but no matching file found on disk")
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val content_type = Files.probeContentType(file_path)
                ?.let { ContentType.parse(it) }
                ?: ContentType.Application.OctetStream

            call.respondBytes(Files.readAllBytes(file_path), content_type)
        }


        post("/covers")
        {
            val schema_name = call.tenant_schema
            var book_id_req: String? = null
            var label_req: String? = null
            var file: ByteArray? = null
            var filename: String? = null

            call
                .receiveMultipart()
                .forEachPart { part ->
                    when (part)
                    {
                        is PartData.FormItem -> {
                            when (part.name)
                            {
                                "book_id" -> book_id_req = part.value
                                "label" -> label_req = part.value
                            }
                        }
                        is PartData.FileItem -> {
                            filename = part.originalFileName
                            file = part.provider().toByteArray()
                        }
                        else -> {}
                    }

                    part.release()
                }

            val book_id = parseUuid(book_id_req).value

            if (book_id == null)
            {
                call.respond(HttpStatusCode.BadRequest, "book_id is not a valid UUID: $book_id_req")
                return@post
            }

            if (file == null)
            {
                call.respond(HttpStatusCode.BadRequest, "you need to provide a cover file")
                return@post
            }

            if (filename == null)
            {
                call.respond(HttpStatusCode.BadRequest, "you need to provide the file name")
                return@post
            }

            val book_context = book_repository.getBookContext(schema_name, book_id)

            if (book_context == null)
            {
                call.respond(HttpStatusCode.BadRequest, "book with ID $book_id does not exist")
                return@post
            }

            val result = cover_repository.create(
                schema_name,
                book_id,
                label_req,
                filename!!,
                file,
                book_context,
            )

            call.respond(result.toCoverResponse())
        }


        patch("/covers/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<CoverUpdateRequest>()

            cover_repository.update(schema_name, id, request.label)

            val result = cover_repository
                .findById(schema_name, id)
                ?.toCoverResponse()

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@patch
            }

            call.respond(result)
        }


        delete("/covers/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@delete

            val is_successful = cover_repository.delete(schema_name, id)

            if (!is_successful)
            {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
