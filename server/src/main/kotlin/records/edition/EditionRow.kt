package dev.jovanni0.records.edition

import dev.jovanni0.data_types.edition.EditionResponse
import java.time.LocalDate
import kotlin.uuid.Uuid



data class EditionRow(
    val id: Uuid,
    val book_id: Uuid,
    val label: String?,
    val release_date: LocalDate?,
    val format: String,
    val added_at: LocalDate,
)
{
    fun toEditionResponse(): EditionResponse
    {
        return EditionResponse(
            id = id.toString(),
            book_id = book_id.toString(),
            label = label,
            release_date = release_date,
            format = format,
            added_at = added_at,
        )
    }
}
