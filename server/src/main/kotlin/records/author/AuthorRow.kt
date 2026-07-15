package dev.jovanni0.records.author

import kotlin.uuid.Uuid



data class AuthorRow(
    val id: Uuid,
    val first_name: String?,
    val last_name: String
)
