package dev.jovanni0.routes

import dev.jovanni0.data_types.PingResponse
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get



fun Route.serverRoutes()
{
    get("/ping")
    {
        val response = PingResponse("CrystalCage Dev Server")

        call.respond(response)
    }
}
