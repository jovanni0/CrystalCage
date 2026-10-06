package dev.jovanni0.routes

import dev.jovanni0.data_types.TagCategoryCreateRequest
import dev.jovanni0.data_types.TagCategoryUpdateRequest
import dev.jovanni0.data_types.tags.TagCreateRequest
import dev.jovanni0.data_types.tags.TagResponse
import dev.jovanni0.data_types.tags.TagUpdateRequest
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.TagCategoryRepository
import dev.jovanni0.repositories.TagRepository
import dev.jovanni0.utils.getUuidCallParamOrRespond
import dev.jovanni0.utils.parseUuid
import dev.jovanni0.utils.parseUuidOrRespond
import dev.jovanni0.utils.parseUuidsOrRespond
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
    tag_category_repository: TagCategoryRepository,
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
                        category_id = it.category_id?.toString(),
                        name = it.name,
                        description = it.description,
                        book_count = it.book_count,
                    )
                }

            call.respond(result)
        }


        get("/tags/categories")
        {
            val schema_name = call.tenant_schema

            val result = tag_category_repository
                .findAll(schema_name)
                .map { it.toTagCategoryResponse() }

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

            val category_id_result = call.tryParseUuidOrRespond(request.category_id, "category_id")

            if (!category_id_result.is_valid)
            {
                call.respond(HttpStatusCode.BadRequest, "category_id is not a valid UUID")
            }

            val result = tag_repository.create(schema_name, category_id_result.value, request.name, request.description)

            call.respond(result.toTagResponse())
        }


        post("/tags/categories")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<TagCategoryCreateRequest>()

            val result = tag_category_repository.create(schema_name, request.name)

            call.respond(result.toTagCategoryResponse())
        }


        patch("/tags/{id}")
        {
            val schema_name = call.tenant_schema
            val tag_id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<TagUpdateRequest>()

            val category_id_result = call.parseUuidOrRespond(request.category_id, "category_id") ?: return@patch
            val added_book_ids = call.parseUuidsOrRespond(request.added_book_ids, "added_book_ids") ?: return@patch
            val removed_book_ids = call.parseUuidsOrRespond(request.removed_book_ids, "removed_book_ids") ?: return@patch

            tag_repository.update(
                schema_name,
                tag_id,
                category_id = category_id_result,
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


        patch("/tags/categories/{id}")
        {
            val schema_name = call.tenant_schema
            val category_id = call.getUuidCallParamOrRespond("id") ?: return@patch
            val request = call.receive<TagCategoryUpdateRequest>()

            val result = tag_category_repository.update(schema_name, category_id, request.name)

            if (result == null)
            {
                call.respond(HttpStatusCode.NotFound, "there is no tag category with ID $category_id")
                return@patch
            }

            call.respond(result.toTagCategoryResponse())
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


        delete("/tags/categories/{id}")
        {
            val schema_name = call.tenant_schema
            val category_id = call.getUuidCallParamOrRespond("id") ?: return@delete

            val rows_deleted = tag_category_repository.delete(schema_name, category_id)

            if (rows_deleted <= 0)
            {
                call.respond(HttpStatusCode.NotFound)
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
