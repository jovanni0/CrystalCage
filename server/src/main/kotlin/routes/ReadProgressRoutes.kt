package dev.jovanni0.routes

import dev.jovanni0.data_types.ReadProgressCreateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.ReadProgressRepository
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.tryParseUuidOrRespond
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post


fun Route.readProgressRoutes(
    read_progress_repository: ReadProgressRepository
)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        get("/read-progress/{record_id}")
        {
            val schema_name = call.tenant_schema
            val read_id = call.getUuidCallParamOrRespond("record_id") ?: return@get

            val result = read_progress_repository.findAllByReadId(schema_name, read_id)

            call.respond(result.map { it.toReadProgressResponse() })
        }

        post("/read-progress")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<ReadProgressCreateRequest>()

            val record_id = call.tryParseUuidOrRespond(request.read_record_id, "read_record_id").value ?: return@post

            if (request.progress !in 0.0..100.0)
            {
                call.respond(HttpStatusCode.BadRequest, "progress has to be between 0 and 100")
                return@post
            }

            val result = read_progress_repository.create(schema_name, record_id, request.progress)

            call.respond(result.toReadProgressResponse())
        }
    }
}