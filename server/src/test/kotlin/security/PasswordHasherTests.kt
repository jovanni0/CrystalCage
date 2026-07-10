package security

import dev.jovanni0.auth.PasswordHasher
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue



class PasswordHasherTests
{
    companion object
    {
        const val PASSWORD = "password"
    }


    @Test
    fun `produces a hash for a password`()
    {
        val hash = PasswordHasher.hash(PASSWORD)

        assertTrue(hash.isNotEmpty())
        assertNotEquals(PASSWORD, hash)
    }


    @Test
    fun `same password produces different hashes`()
    {
        val hash_1 = PasswordHasher.hash(PASSWORD)
        val hash_2 = PasswordHasher.hash(PASSWORD)

        assertNotEquals(hash_1, hash_2)
    }


    @Test
    fun `correct password verifies hash`()
    {
        val hash = PasswordHasher.hash(PASSWORD)
        val is_correct = PasswordHasher.doesMatch(PASSWORD, hash)

        assertTrue(is_correct)
    }


    @Test
    fun `wrong hash fails verification`()
    {
        val hash = PasswordHasher.hash("password123")
        val is_correct = PasswordHasher.doesMatch(PASSWORD, hash)

        assertFalse(is_correct)
    }


    @Test
    fun `special characters work`()
    {
        val password = "Pässw0rd!@#$%^&*"

        val hash = PasswordHasher.hash(password)
        val is_correct = PasswordHasher.doesMatch(password, hash)

        assertTrue(is_correct)
    }


    @Test
    fun `rejects invalid hashes`()
    {
        val hash = "this-is-a-hash"

        val is_correct = PasswordHasher.doesMatch(PASSWORD, hash)

        assertFalse(is_correct)
    }
}
