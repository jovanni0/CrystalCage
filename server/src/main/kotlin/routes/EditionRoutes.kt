package dev.jovanni0.routes

import dev.jovanni0.data_types.edition.EditionUpdateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.repositories.EditionRepository
import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.requiredUuidQueryParam
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.file
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.utils.io.toByteArray
import java.time.LocalDate
import java.time.format.DateTimeParseException



fun Route.editionRoutes(
    book_repository: BookRepository,
    edition_repository: EditionRepository,
)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        get("/editions")
        {
            val schema_name = call.tenant_schema
            val book_id = call.requiredUuidQueryParam("book_id") ?: return@get

            val result = edition_repository.findAllByBookId(schema_name, book_id)

            call.respond(result.map { it.toEditionResponse() })
        }


        post("/editions")
        {
            val schema_name = call.tenant_schema
            var book_id_req: String? = null
            var label: String? = null
            var release_date_req: String? = null
            var format: String?= null
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
                                "label" -> label = part.value
                                "release_date" -> release_date_req = part.value
                                "format" -> format = part.value
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

            var release_date: LocalDate? = null
            if (!release_date_req.isNullOrBlank())
            {
                try
                {
                    release_date = LocalDate.parse(release_date_req)
                }
                catch (e: DateTimeParseException)
                {
                    call.respond(HttpStatusCode.BadRequest, "release_date is an invalid LocalDate\n ${e.message}")
                    return@post
                }
            }


            val book_id = parseUuid(book_id_req).value

            if (book_id == null)
            {
                call.respond(HttpStatusCode.BadRequest, "book_id is not a valid UUID: $book_id_req")
                return@post
            }

            if (format.isNullOrBlank())
            {
                call.respond(HttpStatusCode.BadRequest, "format is required and cannot be null or blank")
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

            val result = edition_repository.create(
                schema_name,
                book_id,
                label,
                release_date,
                format,
                filename!!,
                file,
                book_context,
            )

            call.respond(result.toEditionResponse())
        }


        patch("/editions/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<EditionUpdateRequest>()

            if (request.format is Patch.Value && request.format.value == null)
            {
                call.respond(HttpStatusCode.BadRequest, "format cannot be set to null")
                return@patch
            }

            if (request.label is Patch.Undefined && request.release_date is Patch.Undefined && request.format is Patch.Undefined)
            {
                call.respond(HttpStatusCode.BadRequest, "at least one field has to have a value")
                return@patch
            }

            val result = edition_repository.update(schema_name, id, request.label, request.release_date, request.format)

            // TODO: right now this catches returns for invalid Patch (redundant because of the previous checks). can be removed when Patch is not auto-nullable anymore.
            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound, "there is no edition with ID $id")
                return@patch
            }

            call.respond(result.toEditionResponse())
        }


        delete("/editions/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@delete

            val has_deleted = edition_repository.delete(schema_name, id)

            if (!has_deleted)
            {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
