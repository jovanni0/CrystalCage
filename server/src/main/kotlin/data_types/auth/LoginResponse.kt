package dev.jovanni0.data_types.auth

import kotlinx.serialization.Serializable



@Serializable
data class LoginResponse(
    val id: String,
    val token: String
)
