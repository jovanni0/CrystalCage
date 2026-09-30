package dev.jovanni0.routes

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.net.InetAddress



fun Route.imageProxy(http_client: HttpClient)
{
    authenticate("auth-jwt")
    {
        get("/image-proxy")
        {
            val valid_protocols = listOf("http", "https")
            val target_url_str = call.queryParameters["url"] ?: return@get call.respond(HttpStatusCode.BadRequest, "No URL provided.")

            println(target_url_str)

            val target_url = try
            {
                Url(target_url_str)
            }
            catch (e: Exception)
            {
                println("error why trying to parse URL: ${e.message}")
                return@get call.respond(HttpStatusCode.BadRequest, "Invalid URL.")
            }

            if (target_url.protocol.name !in valid_protocols)
            {
                println("URL has unsupported schema ${target_url.protocol.name}")
                return@get call.respond(HttpStatusCode.BadRequest, "Unsupported URL schema ${target_url.protocol.name}.")
            }

            val address = try
            {
                InetAddress.getByName(target_url.host)
            }
            catch (e: Exception)
            {
                println("could not resolve host ${target_url.host}: ${e.message}")
                return@get call.respond(HttpStatusCode.BadRequest, "Could not resolve host ${target_url.host}.")
            }

            if (address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress || address.isAnyLocalAddress)
            {
                println("host not allowed")
                return@get call.respond(HttpStatusCode.Forbidden, "Host not allowed")
            }

            try
            {
                // NOTE: redirects are automatically followed and could loop back to a local IP. would need to disable `followRedirects` in CIO engine
                //       settings and hand-roll redirect following and bake in the following URL validations.
                val response = http_client.get(target_url)

                val content_type = response.contentType()
                val conten_length = response.contentLength() ?: 0L

                if (!response.status.isSuccess())
                {
                    println("failed to download the resource: ${response.status.description}")
                    return@get call.respond(HttpStatusCode.InternalServerError, "Failed to download resource.")
                }

                if (content_type?.match(ContentType.Image.Any) != true)
                {
                    println("the resource is not an image: ${content_type?.contentType}")
                    return@get call.respond(HttpStatusCode.BadRequest, "URL does not point to an image.")
                }

                if (conten_length > 20 * 1024 * 1024)
                {
                    println("image too large: ${conten_length}")
                    return@get call.respond(HttpStatusCode.BadRequest, "Image is too large.")
                }

                println("sent back image bytes")
                call.respondBytes(response.readRawBytes(), response.contentType())
            }
            catch (e: Exception)
            {
                println("download failed: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, "Resource download failed.")
            }
        }
    }
}
