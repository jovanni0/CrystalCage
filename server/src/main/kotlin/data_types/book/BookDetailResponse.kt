package dev.jovanni0.data_types.book

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class BookDetailResponse(
    val id: String,
    val author_id: String,
    val series_id: String?,
    val universe_id: String?,
    val cover_id: String?,
    val volume_number: Float?,
    val title: String,
    @Serializable(with = LocalDateSerializer::class)
    val launch_date: LocalDate?,
    val word_count: Int?,
    val blurb: String?,
    val notes: String?,
    val tag_ids: List<String>,
    val read_status: ReadStatus,
    val reread_count: Int,
)
