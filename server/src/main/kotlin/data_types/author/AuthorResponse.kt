package dev.jovanni0.data_types.author

import kotlinx.serialization.Serializable



@Serializable
data class AuthorResponse(
    val id: String,
    val first_name: String?,
    val last_name: String,
)
