package dev.jovanni0.routes

import dev.jovanni0.data_types.BookCreateContext
import dev.jovanni0.data_types.CoverCreateContext
import dev.jovanni0.data_types.EditionCreateContext
import dev.jovanni0.data_types.book.BookUpdateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.BookRepository
import dev.jovanni0.utils.Update
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.parseUuidOrRespond
import dev.jovanni0.utils.parseUuidsOrRespond
import dev.jovanni0.utils.toBidDecimal
import dev.jovanni0.utils.uuidQueryParam
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
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



private val INDEXED_FIELD_REGEX = Regex("""^(covers|editions)\[(\d+)]\.(\w+)$""")

private data class CoverPartBuilder(
    var label: String? = null,
    var is_default: Boolean = false,
    var filename: String? = null,
    var file: ByteArray? = null,
)

private data class EditionPartBuilder(
    var label: String? = null,
    var release_date: String? = null,
    var format: String? = null,
    var filename: String? = null,
    var file: ByteArray? = null,
)


fun Route.bookRoutes(
    book_repository: BookRepository,
)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        get("/books")
        {
            val schema_name = call.tenant_schema
            val author_id = call.uuidQueryParam("author_id")
            val universe_id = call.uuidQueryParam("universe_id")
            val series_id = call.uuidQueryParam("series_id")
            val tag_id = call.uuidQueryParam("tag_id")

            val result = book_repository.findAllBy(schema_name, author_id, universe_id, series_id, tag_id)

            call.respond(result.map { it.toBookSummaryResponse() })
        }


        get("/books/{id}")
        {
            val schema_name = call.tenant_schema
            val book_id = call.getUuidCallParamOrRespond("id") ?: return@get

            val result = book_repository.findById(schema_name, book_id)

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(result.toBookDetailResponse())
        }


        post("/books")
        {
            val schema_name = call.tenant_schema

            var author_id_req: String? = null
            var universe_id_req: String? = null
            var series_id_req: String? = null
            var volume_number_req: String? = null
            var title: String? = null
            var launch_date_req: String? = null
            var word_count_req: String? = null
            var blurb: String? = null
            var notes: String? = null
            val tag_ids_req = mutableListOf<String>()

            val cover_builders = mutableMapOf<Int, CoverPartBuilder>()
            val edition_builders = mutableMapOf<Int, EditionPartBuilder>()

            call.receiveMultipart().forEachPart { part ->
                when (part)
                {
                    is PartData.FormItem ->
                    {
                        val indexed = INDEXED_FIELD_REGEX.matchEntire(part.name ?: "")

                        when
                        {
                            part.name == "author_id" -> author_id_req = part.value
                            part.name == "universe_id" -> universe_id_req = part.value
                            part.name == "series_id" -> series_id_req = part.value
                            part.name == "volume_number" -> volume_number_req = part.value
                            part.name == "title" -> title = part.value
                            part.name == "launch_date" -> launch_date_req = part.value
                            part.name == "word_count" -> word_count_req = part.value
                            part.name == "blurb" -> blurb = part.value
                            part.name == "notes" -> notes = part.value
                            part.name == "tag_ids" -> tag_ids_req += part.value

                            indexed != null && indexed.groupValues[1] == "covers" ->
                            {
                                val index = indexed.groupValues[2].toInt()
                                val field = indexed.groupValues[3]
                                val builder = cover_builders.getOrPut(index) { CoverPartBuilder() }

                                when (field)
                                {
                                    "label" -> builder.label = part.value
                                    "is_default" -> builder.is_default = part.value.toBoolean()
                                }
                            }

                            indexed != null && indexed.groupValues[1] == "editions" ->
                            {
                                val index = indexed.groupValues[2].toInt()
                                val field = indexed.groupValues[3]
                                val builder = edition_builders.getOrPut(index) { EditionPartBuilder() }

                                when (field)
                                {
                                    "label" -> builder.label = part.value
                                    "release_date" -> builder.release_date = part.value
                                    "format" -> builder.format = part.value
                                }
                            }
                        }
                    }

                    is PartData.FileItem ->
                    {
                        val indexed = INDEXED_FIELD_REGEX.matchEntire(part.name ?: "")

                        if (indexed != null && indexed.groupValues[1] == "covers" && indexed.groupValues[3] == "file")
                        {
                            val index = indexed.groupValues[2].toInt()
                            val builder = cover_builders.getOrPut(index) { CoverPartBuilder() }
                            builder.filename = part.originalFileName
                            builder.file = part.provider().toByteArray()
                        }
                        else if (indexed != null && indexed.groupValues[1] == "editions" && indexed.groupValues[3] == "file")
                        {
                            val index = indexed.groupValues[2].toInt()
                            val builder = edition_builders.getOrPut(index) { EditionPartBuilder() }
                            builder.filename = part.originalFileName
                            builder.file = part.provider().toByteArray()
                        }
                    }

                    else -> {}
                }

                part.release()
            }

            val author_id = parseUuid(author_id_req).value

            if (author_id == null)
            {
                call.respond(HttpStatusCode.BadRequest, "author_id is not a valid UUID: $author_id_req")
                return@post
            }

            if (title.isNullOrBlank())
            {
                call.respond(HttpStatusCode.BadRequest, "title is required and cannot be blank")
                return@post
            }

            val universe_id = universe_id_req?.let {
                parseUuid(it).value ?: run {
                    call.respond(HttpStatusCode.BadRequest, "universe_id is not a valid UUID: $it")
                    return@post
                }
            }

            val series_id = series_id_req?.let {
                parseUuid(it).value ?: run {
                    call.respond(HttpStatusCode.BadRequest, "series_id is not a valid UUID: $it")
                    return@post
                }
            }

            val volume_number = volume_number_req?.let {
                it.toFloatOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, "volume_number is not a valid number: $it")
                    return@post
                }
            }

            val launch_date = launch_date_req?.let {
                try { LocalDate.parse(it) }
                catch (e: DateTimeParseException)
                {
                    call.respond(HttpStatusCode.BadRequest, "launch_date is not a valid date: $it")
                    return@post
                }
            }

            val word_count = word_count_req?.let {
                it.toIntOrNull() ?: run {
                    call.respond(HttpStatusCode.BadRequest, "word_count is not a valid integer: $it")
                    return@post
                }
            }

            val tag_ids = tag_ids_req.map {
                parseUuid(it).value ?: run {
                    call.respond(HttpStatusCode.BadRequest, "tag_ids contains an invalid UUID: $it")
                    return@post
                }
            }

            val covers = cover_builders.entries.sortedBy { it.key }.map { (index, builder) ->
                if (builder.file == null || builder.filename == null)
                {
                    call.respond(HttpStatusCode.BadRequest, "covers[$index] is missing a file")
                    return@post
                }

                CoverCreateContext(
                    label = builder.label,
                    is_default = builder.is_default,
                    filename = builder.filename!!,
                    file = builder.file!!,
                )
            }

            val default_cover_count = covers.count { it.is_default }

            if (default_cover_count > 1)
            {
                call.respond(HttpStatusCode.BadRequest, "at most one cover can be marked as is_default")
                return@post
            }

            val editions = edition_builders.entries.sortedBy { it.key }.map { (index, builder) ->
                if (builder.file == null || builder.filename == null)
                {
                    call.respond(HttpStatusCode.BadRequest, "editions[$index] is missing a file")
                    return@post
                }

                if (builder.format.isNullOrBlank())
                {
                    call.respond(HttpStatusCode.BadRequest, "editions[$index] is missing a format")
                    return@post
                }

                val release_date = builder.release_date?.let {
                    try { LocalDate.parse(it) }
                    catch (e: DateTimeParseException)
                    {
                        call.respond(HttpStatusCode.BadRequest, "editions[$index].release_date is not a valid date: $it")
                        return@post
                    }
                }

                EditionCreateContext(
                    label = builder.label,
                    release_date = release_date,
                    format = builder.format!!,
                    filename = builder.filename!!,
                    file = builder.file!!,
                )
            }

            val context = BookCreateContext(
                author_id = author_id,
                universe_id = universe_id,
                series_id = series_id,
                volume_number = volume_number,
                title = title,
                launch_date = launch_date,
                word_count = word_count,
                blurb = blurb,
                notes = notes,
                tag_ids = tag_ids,
                covers = covers,
                editions = editions,
            )

            val result = book_repository.create(schema_name, context)

            call.respond(result.toBookSummaryResponse())
        }


        patch("/books/{id}")
        {
            val schema_name = call.tenant_schema
            val book_id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<BookUpdateRequest>()

            val author_id_raw = call.parseUuidOrRespond(request.author_id, "author_id") ?: return@patch
            val universe_id = call.parseUuidOrRespond(request.universe_id, "universe_id") ?: return@patch
            val series_id = call.parseUuidOrRespond(request.series_id, "series_id") ?: return@patch
            val cover_id = call.parseUuidOrRespond(request.cover_id, "cover_id") ?: return@patch
            val tag_ids = call.parseUuidsOrRespond(request.tag_ids, "tag_ids") ?: return@patch

            val author_id = when (author_id_raw)
            {
                is Update.Undefined -> Update.Undefined
                is Update.Value -> {
                    if (author_id_raw.value == null)
                    {
                        call.respond(HttpStatusCode.BadRequest, "author_id field cannot be set to null")
                        return@patch
                    }

                    Update.Value(author_id_raw.value)
                }
            }

            val result = book_repository.update(
                schema_name,
                book_id,
                author_id,
                universe_id,
                series_id,
                cover_id,
                request.volume_number.toBidDecimal(),
                request.title,
                request.launch_date,
                request.word_count,
                request.blurb,
                request.notes,
                tag_ids
            )

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@patch
            }

            call.respond(result.toBookDetailResponse())
        }


        delete("/books/{id}")
        {
            val schema_name = call.tenant_schema
            val book_id = call.getUuidCallParamOrRespond("id") ?: return@delete

            val was_deleted = book_repository.delete(schema_name, book_id)

            if (!was_deleted)
            {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
