package dev.jovanni0.data_types.tags

import kotlinx.serialization.Serializable



@Serializable
data class TagDetailResponse(
    val id: String,
    val name: String,
    val description: String?,
    val books: List<TagBookItem>
)
