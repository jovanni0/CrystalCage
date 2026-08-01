package dev.jovanni0.records.universe

import kotlin.uuid.Uuid



data class UniverseRow(
    val id: Uuid,
    val author_id: Uuid,
    val name: String,
)
