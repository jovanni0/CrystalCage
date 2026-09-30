package dev.jovanni0.records

import java.time.LocalDate
import kotlin.uuid.Uuid



data class UserRecord(
    val id: Uuid,
    val username: String,
    val password_hash: String,
    val role: String,
    val created_at: LocalDate
)
