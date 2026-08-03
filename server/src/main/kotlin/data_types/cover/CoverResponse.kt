package dev.jovanni0.data_types.cover


import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class CoverResponse(
    val id: String,
    val book_id: String,
    val label: String?,
    @Serializable(with = LocalDateSerializer::class)
    val added_at: LocalDate,
    val is_default: Boolean,
)
