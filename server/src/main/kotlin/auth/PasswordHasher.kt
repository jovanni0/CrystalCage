package dev.jovanni0.auth

import at.favre.lib.crypto.bcrypt.BCrypt



object PasswordHasher
{
    fun hash(plaintext_password: String): String
    {
        val hashed_password = BCrypt
            .withDefaults()
            .hashToString(12, plaintext_password.toCharArray())

        return hashed_password
    }


    fun doesMatch(plaintext_password: String, hashed_password: String): Boolean
    {
        val result = BCrypt
            .verifyer()
            .verify(plaintext_password.toCharArray(), hashed_password.toCharArray())

        return result.verified
    }
}
