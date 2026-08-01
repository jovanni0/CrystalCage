package dev.jovanni0.data_types.series

import kotlinx.serialization.Serializable



@Serializable
data class SeriesResponse(
    val id: String,
    val author_id: String,
    val universe_id: String?,
    val name: String,
    val is_finished: Boolean,
)
