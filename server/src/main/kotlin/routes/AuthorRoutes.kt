package dev.jovanni0.routes

import dev.jovanni0.data_types.author.AuthorCreateRequest
import dev.jovanni0.data_types.author.AuthorResponse
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.AuthorRepository
import dev.jovanni0.utils.getUuidCallParamOrRespond
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post



fun Route.authorRoutes(author_repository: AuthorRepository)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)


        get("/authors")
        {
            val schema_name = call.tenant_schema

            println("got GET /authors request")

            val authors = author_repository
                .findAll(schema_name)
                .map {
                    AuthorResponse(
                        id = it.id.toString(),
                        first_name = it.first_name,
                        last_name = it.last_name
                    )
                }

            call.respond(authors)

            println("responded to GET /authors request")
        }


        get("/authors/{id}")
        {
            val schema_name = call.tenant_schema
            val id = call.getUuidCallParamOrRespond("id") ?: return@get

            val author = author_repository.findById(schema_name, id)

            if (author == null)
            {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            call.respond(author.toAuthorResponse())
        }


        post("/authors")
        {
            val schema_name = call.tenant_schema
            val request = call.receive<AuthorCreateRequest>()

            if (request.last_name.isBlank())
            {
                call.respond(HttpStatusCode.BadRequest, "last_name is required")
                return@post
            }

            val created_author = author_repository.create(schema_name, request.first_name, request.last_name)

            call.respond(
                HttpStatusCode.Created,
                AuthorResponse(
                    id = created_author.id.toString(),
                    first_name = created_author.first_name,
                    last_name = created_author.last_name
                )
            )
        }
    }
}
