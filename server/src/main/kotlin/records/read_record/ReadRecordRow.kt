package dev.jovanni0.records.read_record

import java.time.LocalDate
import kotlin.uuid.Uuid



data class ReadRecordRow(
    val id: Uuid,
    val book_id: Uuid,
    val edition_id: Uuid?,
    val start_date: LocalDate?,
    val end_date: LocalDate?,
    val did_not_finish: Boolean,
    val is_historical: Boolean,
    val stop_position: String?,
    val notes: String?
)
