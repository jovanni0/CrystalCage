package dev.jovanni0.data_types

import kotlinx.serialization.Serializable



@Serializable
data class TagCategoryCreateRequest(
    val name: String,
)
