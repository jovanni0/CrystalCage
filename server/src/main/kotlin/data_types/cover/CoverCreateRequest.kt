package dev.jovanni0.data_types.cover



data class CoverCreateRequest(
    val book_id: String,
    val label: String? = null,
)
