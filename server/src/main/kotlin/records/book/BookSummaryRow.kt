package dev.jovanni0.records.book

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.data_types.book.BookSummaryResponse
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
{
    fun toBookSummaryResponse(): BookSummaryResponse
    {
        return BookSummaryResponse(
            id = id.toString(),
            author_id = author_id.toString(),
            universe_id = universe_id?.toString(),
            series_id = series_id?.toString(),
            cover_id = cover_id?.toString(),
            volume_number = volume_number,
            title = title,
            read_status = read_status,
            reread_count = reread_count,
        )
    }
}
