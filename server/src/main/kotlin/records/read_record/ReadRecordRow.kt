package dev.jovanni0.records.read_record

import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlin.uuid.Uuid



@Serializable
data class ReadRecordRow(
    val id: Uuid,
    val book_id: Uuid,
    val edition_id: Uuid?,
    @Serializable(with = LocalDateSerializer::class)
    val start_date: LocalDate?,
    @Serializable(with = LocalDateSerializer::class)
    val end_date: LocalDate?,
    val did_not_finish: Boolean,
    val is_historical: Boolean,
    val stop_position: String?,
    val notes: String?
)
