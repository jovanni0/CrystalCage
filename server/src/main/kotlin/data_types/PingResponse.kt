package dev.jovanni0.data_types

import kotlinx.serialization.Serializable


@Serializable
data class PingResponse(
    val server_name: String
)
