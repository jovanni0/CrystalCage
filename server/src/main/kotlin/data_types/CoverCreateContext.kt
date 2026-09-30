package dev.jovanni0.data_types



data class CoverCreateContext(
    val label: String? = null,
    val is_default: Boolean = false,
    val filename: String,
    val file: ByteArray,
)
