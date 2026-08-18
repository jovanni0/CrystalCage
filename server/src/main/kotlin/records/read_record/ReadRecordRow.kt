package dev.jovanni0.records.read_record

import dev.jovanni0.data_types.read_record.ReadRecordResponse
import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
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
{
    fun toReadRecordResponse(): ReadRecordResponse
    {
        return ReadRecordResponse(
            id = this.id.toString(),
            book_id = this.book_id.toString(),
            edition_id = this.edition_id?.toString(),
            start_date = this.start_date,
            end_date = this.end_date,
            did_not_finish = this.did_not_finish,
            is_historical = this.is_historical,
            stop_position = this.stop_position,
            notes = this.notes,
        )
    }
}
