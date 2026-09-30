package dev.jovanni0.routes

import dev.jovanni0.data_types.series.SeriesCreateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.records.series.SeriesRow
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.repositories.SeriesRepository
import dev.jovanni0.repositories.UniverseRepository
import dev.jovanni0.utils.parseUuid
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post



fun Route.seriesRoutes(
    author_repository: AuthorRepository,
    universe_repository: UniverseRepository,
    series_repository: SeriesRepository,
) {
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        // GET /series(?author_id=...&universe_id=...)
        get("/series")
        {
            val schema_name = call.tenant_schema
            val author_id_param = call.queryParameters["author_id"]
            val universe_id_param = call.queryParameters["universe_id"]
            val author_id_result = parseUuid(author_id_param)
            val universe_id_result = parseUuid(universe_id_param)

            if (!author_id_result.is_valid)
            {
                call.respond(HttpStatusCode.BadRequest, "author_id is not a valid UUID")
                return@get
            }

            if (!universe_id_result.is_valid)
            {
                call.respond(HttpStatusCode.BadRequest, "universe_id is not a valid UUID")
                return@get
            }

            val result = series_repository.findAllByAuthorIdAndUniverseId(schema_name, author_id_result.value, universe_id_result.value)

            call.respond(result.map { it.toSeriesResponse() })
        }


        get("/series/{id}")
        {
            val schema_name = call.tenant_schema
            val series_id_param = call.parameters["id"]
            val series_id_result = parseUuid(series_id_param)

            if (series_id_param == null)
            {
                call.respond(HttpStatusCode.BadRequest, "id is required")
                return@get
            }

            if (series_id_result.value == null)
            {
                call.respond(HttpStatusCode.BadRequest, "id is not a valid UUID")
                return@get
            }

            val result = series_repository.findById(schema_name, series_id_result.value)

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(result.toSeriesDetailResponse())
        }


        post("/series")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<SeriesCreateRequest>()
            val author_uuid_result = parseUuid(request.author_id)
            val universe_uuid_result = parseUuid(request.universe_id)

            if (request.name.isBlank())
            {
                call.respond(HttpStatusCode.BadRequest, "name cannot be blank")
                return@post
            }

            if (author_uuid_result.value == null)
            {
                call.respond(HttpStatusCode.BadRequest, "author_id is not a valid UUID")
                return@post
            }

            if (request.universe_id != null && universe_uuid_result.value == null)
            {
                call.respond(HttpStatusCode.BadRequest, "universe_id is not a valid UUID")
                return@post
            }

            if (!author_repository.exists(schema_name,author_uuid_result.value))
            {
                call.respond(HttpStatusCode.BadRequest, "author with ID ${author_uuid_result.value} does not exist")
                return@post
            }

            if (request.universe_id != null)
            {
                val universe_record = universe_repository.findById(schema_name, universe_uuid_result.value!!)

                if (universe_record == null)
                {
                    call.respond(HttpStatusCode.BadRequest, "universe with ID ${universe_uuid_result.value} does not exist")
                    return@post
                }

                if (universe_record.author_id != author_uuid_result.value)
                {
                    call.respond(HttpStatusCode.BadRequest, "author_id and universe.author_id do not match: '${author_uuid_result.value}' and '${universe_record.author_id}'")
                    return@post
                }
            }

            val result = series_repository.create(
                schema_name,
                author_uuid_result.value,
                universe_uuid_result.value,
                request.name,
                request.is_finished == true
            )

            call.respond(HttpStatusCode.Created, result.toSeriesResponse())
        }
    }
}
