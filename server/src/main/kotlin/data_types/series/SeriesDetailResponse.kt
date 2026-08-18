package dev.jovanni0.data_types.series

import dev.jovanni0.data_types.book.BookSummaryResponse
import kotlinx.serialization.Serializable



@Serializable
data class SeriesDetailResponse(
    val id: String,
    val author_id: String,
    val universe_id: String?,
    val name: String,
    val is_finished: Boolean,
    val books: List<SeriesBookItem>
)
