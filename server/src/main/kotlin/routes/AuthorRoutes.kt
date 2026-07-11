package dev.jovanni0.routes

import dev.jovanni0.data_types.author.AuthorResponse
import dev.jovanni0.database.TenantResolution
import dev.jovanni0.database.tenant_schema
import dev.jovanni0.repositories.AuthorRepository
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get



fun Route.authorRoutes(author_repository: AuthorRepository)
{
    authenticate("auth-jwt")
    {
        install(TenantResolution)

        get("/author")
        {
            val schema_name = call.tenant_schema

            val authors = author_repository
                .findAll(schema_name)
                .map {
                    AuthorResponse(
                        id = it.id,
                        first_name = it.first_name,
                        last_name = it.last_name
                    )
                }

            call.respond(authors)
        }
    }
}
