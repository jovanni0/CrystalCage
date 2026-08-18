package dev.jovanni0.records.book

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.data_types.book.BookDetailResponse
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
{
    fun toBookDetailResponse(): BookDetailResponse
    {
        return BookDetailResponse (
            id = id.toString(),
            author_id = author_id.toString(),
            series_id = series_id?.toString(),
            universe_id = universe_id?.toString(),
            cover_id = cover_id?.toString(),
            volume_number = volume_number,
            title = title,
            launch_date = launch_date,
            word_count = word_count,
            blurb = blurb,
            notes = notes,
            tag_ids = tag_ids.map { it.toString() },
            read_status = read_status,
            reread_count = reread_count,
        )
    }
}
