package dev.jovanni0.records.tags

import kotlin.uuid.Uuid



data class TagCountRow(
    val id: Uuid,
    val category_id: Uuid?,
    val name: String,
    val description: String?,
    val book_count: Int,
)
