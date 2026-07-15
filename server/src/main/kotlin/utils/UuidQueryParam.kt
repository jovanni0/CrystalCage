package dev.jovanni0.utils

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import kotlin.uuid.Uuid



data class UuidParseResult(
    val value: Uuid?,
    val is_valid: Boolean
)


fun parseUuid(value: String?): UuidParseResult
{
    if (value == null)
    {
        return UuidParseResult(
            value = null,
            is_valid = true
        )
    }

    return try
    {
        UuidParseResult(
            value = Uuid.parse(value),
            is_valid = true
        )
    }
    catch (e: IllegalArgumentException)
    {
        UuidParseResult(
            value = null,
            is_valid = false
        )
    }
}



/**
 * extract and parse `Uuid` form query parameters.
 * if the parameter is not a valid `Uuid` respond with `BadRequest` and return `null`.
 */
suspend fun ApplicationCall.uuidQueryParam(param_name: String): Uuid?
{
    val param_value = request.queryParameters[param_name] ?: return null

    return try
    {
        Uuid.parse(param_value)
    }
    catch (e: IllegalArgumentException)
    {
        respond(HttpStatusCode.BadRequest, "$param_name is not a valid UUID")

        return null
    }
}


/**
 * extract and parse required `Uuid` form query parameters.
 * if the parameter is not found or is not a valid `Uuid` respond with `BadRequest` and return `null`.
 */
suspend fun ApplicationCall.requiredUuidQueryParam(param_name: String): Uuid?
{
    val param_value = request.queryParameters[param_name]

    if (param_value == null)
    {
        respond(HttpStatusCode.BadRequest, "$param_name is required but was not provided")

        return null
    }

    return try
    {
        Uuid.parse(param_value)
    }
    catch (e: IllegalArgumentException)
    {
        respond(HttpStatusCode.BadRequest, "$param_name is not a valid UUID")

        return null
    }
}


/**
 * extract and parse Uuid form call parameters.
 */
suspend fun ApplicationCall.getUuidCallParamOrRespond(param_name: String): Uuid?
{
    val param_value = request.call.parameters[param_name] ?: return null

    return try {
        Uuid.parse(param_value)
    }
    catch (e: IllegalArgumentException)
    {
        respond(HttpStatusCode.BadRequest, "$param_name is not a valid UUID")

        null
    }
}


/**
 * try parse the string to Uuid. if the format is invalid, return `BadRequest`; if the value is `null`, return `null`.
 */
suspend fun ApplicationCall.tryParseUuidOrRespond(value: String?, param_name: String): UuidParseResult
{
    val result = parseUuid(value)

    if (!result.is_valid)
    {
        respond(HttpStatusCode.BadRequest, "$param_name is not a valid UUID")
    }

    return result
}
