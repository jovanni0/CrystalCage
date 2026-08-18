package dev.jovanni0.records.series

import dev.jovanni0.data_types.book.BookSummaryResponse
import dev.jovanni0.data_types.series.SeriesDetailResponse
import kotlin.uuid.Uuid



data class SeriesDetailRow(
    val id: Uuid,
    val author_id: Uuid,
    val universe_id: Uuid?,
    val name: String,
    val is_finished: Boolean,
    val books: List<SeriesBookRow>
)
{
    fun toSeriesDetailResponse(): SeriesDetailResponse
    {
        return SeriesDetailResponse(
            id = this.id.toString(),
            author_id = this.author_id.toString(),
            universe_id = this.universe_id?.toString(),
            name = this.name,
            is_finished = this.is_finished,
            books = this.books.map { it.toSeriesBookItem() },
        )
    }
}
