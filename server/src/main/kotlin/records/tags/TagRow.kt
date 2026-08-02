package dev.jovanni0.records.tags

import kotlin.uuid.Uuid



data class TagRow(
    val id: Uuid,
    val name: String,
    val description: String?,
)
