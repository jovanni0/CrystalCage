package dev.jovanni0.data_types

import kotlinx.serialization.Serializable


@Serializable
data class ReadProgressCreateRequest(
    val read_record_id: String,
    val progress: Double
)
