package dev.jovanni0.data_types

import java.time.LocalDate



data class EditionCreateContext(
    val label: String? = null,
    val release_date: LocalDate? = null,
    val format: String,
    val filename: String,
    val file: ByteArray,
)
