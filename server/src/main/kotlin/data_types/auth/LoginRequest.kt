package dev.jovanni0.data_types.auth

import kotlinx.serialization.Serializable



@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)