package utils.patch

import dev.jovanni0.utils.Patch
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame



class PatchTests
{
    @Test
    fun `Undefined is a singleton`()
    {
        val patch_1 = Patch.Undefined
        val patch_2 = Patch.Undefined

        assertSame(patch_1, patch_2)
    }

    @Test
    fun `Value holding null is distinguishable from Value holding a real value`()
    {
        val cleared: Patch<String> = Patch.Value(null)
        val set: Patch<String> = Patch.Value("x")

        assertTrue(cleared is Patch.Value)
        assertTrue(set is Patch.Value)
        assertNull((cleared as Patch.Value).value)
        assertEquals("x", (set as Patch.Value).value)
    }

    @Test
    fun `Value is distinguishable from Undefined via is-check`()
    {
        val undefined: Patch<String> = Patch.Undefined
        val value: Patch<String> = Patch.Value("x")

        assertFalse(undefined is Patch.Value)
        assertTrue(value is Patch.Value)
    }

    @Test
    fun `Value equality holds for equal contents`()
    {
        val a: Patch<String> = Patch.Value("x")
        val b: Patch<String> = Patch.Value("x")

        assertEquals(a, b)
    }

    @Test
    fun `Value equality fails for different contents`()
    {
        val a: Patch<String> = Patch.Value("x")
        val b: Patch<String> = Patch.Value("y")

        assertNotEquals(a, b)
    }

    @Test
    fun `Value(null) equals another Value(null)`()
    {
        val a: Patch<String> = Patch.Value(null)
        val b: Patch<String> = Patch.Value(null)

        assertEquals(a, b)
    }

    @Test
    fun `Value(null) does not equal Value with a real value`()
    {
        val a: Patch<String> = Patch.Value(null)
        val b: Patch<String> = Patch.Value("x")

        assertNotEquals(a, b)
    }

    @Test
    fun `exhaustive when over Patch requires no else branch`()
    {
        // this is a compile-time guarantee from `sealed class` — the test
        // itself is trivial, but its real value is that it fails to compile
        // if Patch ever gains a third subtype without updating call sites.
        fun describe(patch: Patch<String>): String = when (patch)
        {
            is Patch.Undefined -> "undefined"
            is Patch.Value -> "value:${patch.value}"
        }

        assertEquals("undefined", describe(Patch.Undefined))
        assertEquals("value:x", describe(Patch.Value("x")))
        assertEquals("value:null", describe(Patch.Value(null)))
    }
}
