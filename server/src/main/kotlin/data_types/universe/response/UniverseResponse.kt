package dev.jovanni0.data_types.universe.response

import kotlinx.serialization.Serializable



@Serializable
data class UniverseResponse(
    val id: String,
    val author_id: String,
    val name: String,
)
