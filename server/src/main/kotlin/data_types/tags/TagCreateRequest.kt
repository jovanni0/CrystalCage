package dev.jovanni0.data_types.tags

import kotlinx.serialization.Serializable



@Serializable
data class TagCreateRequest(
    val name: String,
    val description: String,
)
