package dev.jovanni0.data_types.edition

import java.time.LocalDate



data class EditionResponse(
    val id: String,
    val book_id: String,
    val label: String?,
    val release_date: LocalDate?,
    val format: String,
    val added_at: LocalDate,
)
