package dev.jovanni0.records.series

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid



@Serializable
data class SeriesRow(
    val id: Uuid,
    val author_id: Uuid,
    val universe_id: Uuid?,
    val name: String,
    val is_finished: Boolean,
)
