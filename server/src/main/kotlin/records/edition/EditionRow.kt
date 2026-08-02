package dev.jovanni0.records.edition

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
