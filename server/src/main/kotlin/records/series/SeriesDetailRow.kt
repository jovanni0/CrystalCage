package dev.jovanni0.records.series

import dev.jovanni0.data_types.book.BookSummaryResponse
import kotlin.uuid.Uuid



data class SeriesDetailRow(
    val id: Uuid,
    val author_id: Uuid,
    val universe_id: Uuid?,
    val name: String,
    val is_finished: Boolean,
    val books: List<BookSummaryResponse>
)
