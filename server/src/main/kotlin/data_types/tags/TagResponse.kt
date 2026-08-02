package dev.jovanni0.data_types.tags

import kotlinx.serialization.Serializable



@Serializable
data class TagResponse(
    val id: String,
    val name: String,
    val description: String?,
    val book_count: Int,
)
