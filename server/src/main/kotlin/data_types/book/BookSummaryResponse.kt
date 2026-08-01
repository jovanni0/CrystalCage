package dev.jovanni0.data_types.book

import dev.jovanni0.data_types.ReadStatus
import kotlinx.serialization.Serializable



@Serializable
data class BookSummaryResponse(
    val id: String,
    val author_id: String,
    val series_id: String?,
    val universe_id: String?,
    val cover_id: String?,
    val volume_number: Float?,
    val title: String,
    val read_status: ReadStatus,
    val reread_count: Int,
)
