package dev.jovanni0.database

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.util.AttributeKey



val TenantSchemaKey = AttributeKey<String>("TenantSchema")



/**
 * handles schema resolution based on the user ID.
 */
val TenantResolution = createRouteScopedPlugin("TenantResolution")
{
    on(AuthenticationChecked)
    { call ->
        val principal = call.principal<JWTPrincipal>()
        val user_id = principal?.payload?.getClaim("user_id")?.asString()
        val valid_id_regex = "^[a-zA-Z0-9-]+$".toRegex()

        if (user_id == null || !user_id.matches(valid_id_regex) || user_id.length > 45)
        {
            println("errored, $user_id")
            call.respond(HttpStatusCode.Unauthorized)
            return@on
        }

        call.attributes.put(TenantSchemaKey, "user_$user_id")
    }
}



val ApplicationCall.tenant_schema: String
    get() = attributes[TenantSchemaKey]
