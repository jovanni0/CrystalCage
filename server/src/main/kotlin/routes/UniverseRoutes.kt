package dev.jovanni0.routes

import dev.jovanni0.data_types.universe.request.UniverseCreateRequest
import dev.jovanni0.data_types.universe.response.UniverseResponse
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.records.universe.UniverseRow
import dev.jovanni0.repositories.UniverseRepository
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.requiredUuidQueryParam
import dev.jovanni0.utils.tryParseUuidOrRespond
import dev.jovanni0.utils.uuidQueryParam
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post



fun Route.universeRoutes(universe_repository: UniverseRepository)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        // GET /universes(?author_id=...)
        get("/universes")
        {
            val schema_name = call.tenant_schema
//            val author_id = call.requiredUuidQueryParam("author_id")

            val author_id_param = call.queryParameters["author_id"]
            val author_id_req = parseUuid(author_id_param)

            if (!author_id_req.is_valid)
            {
                call.respond(HttpStatusCode.BadRequest, "author_id is not a valid UUID $author_id_param")
                return@get
            }

            var universes: List<UniverseRow> = emptyList()
            if (author_id_req.value === null)
            {
                universes = universe_repository.findAll(schema_name)
            }
            else
            {
                universes = universe_repository.findAllByAuthorId(schema_name, author_id_req.value)
            }

            call.respond(universes.map { it.toUniverseResponse() })
        }


        post("/universes")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<UniverseCreateRequest>()

            val request_validation_error = request.validate()
            if (request_validation_error != null)
            {
                call.respond(HttpStatusCode.BadRequest, request_validation_error)
                return@post
            }

            val author_id_result = call.tryParseUuidOrRespond(request.author_id, "author_id")

            if (!author_id_result.is_valid || author_id_result.value == null)
            {
                return@post
            }

            val new_universe = universe_repository.create(schema_name, request.name, author_id_result.value)

            call.respond(
                UniverseResponse(
                    id = new_universe.id.toString(),
                    author_id = new_universe.author_id.toString(),
                    name = new_universe.name,
                )
            )
        }
    }
}
