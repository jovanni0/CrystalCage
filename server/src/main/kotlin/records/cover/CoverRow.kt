package dev.jovanni0.records.cover

import dev.jovanni0.data_types.cover.CoverResponse
import java.time.LocalDate
import kotlin.uuid.Uuid



data class CoverRow(
    val id: Uuid,
    val book_id: Uuid,
    val label: String?,
    val added_at: LocalDate,
    val is_default: Boolean,
)
{
    fun toCoverResponse(): CoverResponse
    {
        return CoverResponse(
            id = id.toString(),
            book_id = book_id.toString(),
            label = label,
            added_at = added_at,
            is_default = is_default
        )
    }
}
