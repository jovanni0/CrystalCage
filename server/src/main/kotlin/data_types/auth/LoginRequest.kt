package dev.jovanni0.data_types.auth

import kotlinx.serialization.Serializable



@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)
{
    fun validate(): Boolean
    {
        return username.isNotBlank() && password.isNotBlank() && password.length > 3
    }
}
