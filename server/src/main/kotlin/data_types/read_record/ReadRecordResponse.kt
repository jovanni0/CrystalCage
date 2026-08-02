package dev.jovanni0.data_types.read_record

import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class ReadRecordResponse(
    val id: String,
    val book_id: String,
    val edition_id: String?,
    @Serializable(with = LocalDateSerializer::class)
    val start_date: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val end_date: LocalDate?,
    val did_not_finish: Boolean,
    val is_historical: Boolean,
    val stop_position: String?,
    val notes: String?,
)
