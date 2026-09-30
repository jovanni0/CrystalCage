package dev.jovanni0.data_types.series

import kotlinx.serialization.Serializable



@Serializable
data class SeriesCreateRequest(
    val author_id:   String,
    val universe_id: String? = null,
    val name:        String,
    val is_finished: Boolean? = null,
)
