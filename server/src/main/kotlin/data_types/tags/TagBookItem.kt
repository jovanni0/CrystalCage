package dev.jovanni0.data_types.tags

import kotlinx.serialization.Serializable



@Serializable
data class TagBookItem(
    val id: String,
    val title: String,
    val series_id: String?,
    val cover_id: String?,
)
