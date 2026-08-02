package dev.jovanni0.routes

import dev.jovanni0.data_types.read_record.ReadRecordCreateRequest
import dev.jovanni0.data_types.read_record.ReadRecordUpdateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.ReadRecordRepository
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.parsePatchUuidOrRespond
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.requiredUuidQueryParam
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post



fun Route.readRecordRoutes(
    read_record_repo: ReadRecordRepository,
) {
    authenticate("auth-jwt")
    {
        install(TenantResolution)



        get("/read-records")
        {
            val schema_name = call.tenant_schema
            val book_id = call.requiredUuidQueryParam("book_id") ?: return@get

            val result = read_record_repo.findAllByBookId(schema_name, book_id)

            call.respond(result)
        }


        get("/read-records/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@get

            val result = read_record_repo.findById(schema_name, id)

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(result)
        }


        post("/read-records")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<ReadRecordCreateRequest>()

            val request_validation_error = request.validateDateLogic()
            if (request_validation_error != null)
            {
                call.respond(HttpStatusCode.BadRequest, request_validation_error)
                return@post
            }

            val book_id_result = parseUuid(request.book_id)
            if (book_id_result.value == null)
            {
                call.respond(HttpStatusCode.BadRequest, "book_id is not a valid UUID")
                return@post
            }

            val edition_id_result = parseUuid(request.edition_id)
            if (request.edition_id != null && edition_id_result.value == null)
            {
                call.respond(HttpStatusCode.BadRequest, "edition_id is not a valid UUID")
                return@post
            }

            val result = read_record_repo.create(
                schema_name,
                book_id_result.value,
                edition_id_result.value,
                request.start_date,
                request.end_date,
                request.did_not_finish,
                request.is_historical,
                request.stop_position,
                request.notes
            )

            call.respond(HttpStatusCode.Created, result)
        }


        patch("/read-records/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<ReadRecordUpdateRequest>()

            val edition_id = call.parsePatchUuidOrRespond(request.edition_id, "edition_id") ?: return@patch

            val result = read_record_repo.update(
                schema_name,
                id,
                edition_id,
                request.start_date,
                request.end_date,
                request.did_not_finish,
                request.is_historical,
                request.stop_position,
                request.notes,
            )

            if (result == 0)
            {
                call.respond(HttpStatusCode.NotFound)
                return@patch
            }
            else if (result == null)
            {
                call.respond(HttpStatusCode.BadRequest, "at least one field has to have a value")
                return@patch
            }

            val new_record = read_record_repo.findById(schema_name, id)

            if (new_record == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@patch
            }

            call.respond(new_record)
        }


        delete("/read-records/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@delete

            val result = read_record_repo.delete(schema_name, id)

            if (result == 0)
            {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
