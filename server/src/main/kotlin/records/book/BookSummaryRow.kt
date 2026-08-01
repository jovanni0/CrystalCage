package dev.jovanni0.records.book

import dev.jovanni0.data_types.ReadStatus
import kotlin.uuid.Uuid



data class BookSummaryRow(
    val id: Uuid,
    val author_id: Uuid,
    val series_id: Uuid?,
    val universe_id: Uuid?,
    val cover_id: Uuid?,
    val volume_number: Float?,
    val title: String,
    val read_status: ReadStatus,
    val reread_count: Int,
)
