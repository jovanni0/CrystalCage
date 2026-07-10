package dev.jovanni0.records

import java.time.LocalDate



data class UserRecord(
    val id: String,
    val username: String,
    val password_hash: String,
    val role: String,
    val created_at: LocalDate
)
