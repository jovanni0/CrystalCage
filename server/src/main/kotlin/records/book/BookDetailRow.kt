package dev.jovanni0.records.book

import dev.jovanni0.data_types.ReadStatus
import java.time.LocalDate
import kotlin.uuid.Uuid



data class BookDetailRow(
    val id: Uuid,
    val author_id: Uuid,
    val series_id: Uuid?,
    val universe_id: Uuid?,
    val cover_id: Uuid?,
    val volume_number: Float?,
    val title: String,
    val launch_date: LocalDate?,
    val word_count: Int?,
    val blurb: String?,
    val notes: String?,
    val tag_ids: List<Uuid>,
    val read_status: ReadStatus,
    val reread_count: Int,
)
