package dev.jovanni0.data_types.auth

import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class MeResponse(
    val id: String,
    val username: String,
    val role: String,

    @Serializable(with = LocalDateSerializer::class)
    val created_at: LocalDate,
)
