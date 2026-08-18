package dev.jovanni0.routes

import dev.jovanni0.data_types.tags.TagCreateRequest
import dev.jovanni0.data_types.tags.TagResponse
import dev.jovanni0.data_types.tags.TagUpdateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.TagRepository
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.parseUuidsOrRespond
import dev.jovanni0.utils.requiredUuidQueryParam
import dev.jovanni0.utils.tryParseUuidOrRespond
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post


fun Route.tagRoutes(
    tag_repository: TagRepository,
) {
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        get("/tags")
        {
            val schema_name = call.tenant_schema

            val result = tag_repository
                .findAll(schema_name)
                .map {
                    TagResponse(
                        id = it.id.toString(),
                        name = it.name,
                        description = it.description,
                        book_count = it.book_count,
                    )
                }

            call.respond(result)
        }


        get("/tags/{id}")
        {
            val schema_name = call.tenant_schema
            val tag_id = call.getUuidCallParamOrRespond("id") ?: return@get

            val result = tag_repository
                .findById(schema_name, tag_id)
                ?.toTagDetailResponse()

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(result)
        }


        post("/tags")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<TagCreateRequest>()

            val result = tag_repository.create(schema_name, request.name, request.description)

            call.respond(result.toTagResponse())
        }


        patch("/tags/{id}")
        {
            val schema_name = call.tenant_schema
            val tag_id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<TagUpdateRequest>()

            val added_book_ids = call.parseUuidsOrRespond(request.added_book_ids, "added_book_ids") ?: return@patch
            val removed_book_ids = call.parseUuidsOrRespond(request.removed_book_ids, "removed_book_ids") ?: return@patch

            tag_repository.update(
                schema_name,
                tag_id,
                request.name,
                request.description,
                added_book_ids,
                removed_book_ids,
            )

            val result = tag_repository
                .findById(schema_name, tag_id)
                ?.toTagDetailResponse()

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound, "there is no tag with ID $tag_id")
                return@patch
            }

            call.respond(result)
        }


        delete("/tags/{id}")
        {
            val schema_name = call.tenant_schema
            val tag_id = call.getUuidCallParamOrRespond("id") ?: return@delete

            val rows_deleted = tag_repository.delete(schema_name, tag_id)

            if (rows_deleted <= 0)
            {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
