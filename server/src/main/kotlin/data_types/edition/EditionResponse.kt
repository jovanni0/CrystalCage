package dev.jovanni0.data_types.edition

import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class EditionResponse(
    val id: String,
    val book_id: String,
    val label: String?,
    @Serializable(with = LocalDateSerializer::class)
    val release_date: LocalDate?,
    val format: String,
    @Serializable(with = LocalDateSerializer::class)
    val added_at: LocalDate,
)
