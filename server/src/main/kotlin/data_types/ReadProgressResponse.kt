package dev.jovanni0.data_types

import kotlinx.serialization.Serializable


@Serializable
data class ReadProgressResponse(
    val id: String,
    val read_record_id: String,
    val recorded_at: String,
    val progress: Double
)
