package utils.patch

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.PatchStringSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals



class PatchSerializerTests
{
    @Test
    fun `serializing Undefined throws`()
    {
        assertThrows<SerializationException> {
            Json.encodeToString(PatchStringSerializer, Patch.Undefined)
        }
    }


    @Test
    fun `serializing Value(null) produces JSON null`()
    {
        val result = Json.encodeToString(PatchStringSerializer, Patch.Value(null))

        assertEquals("null", result)
    }


    @Test
    fun `serializing Value with a real value produces that value`()
    {
        val result = Json.encodeToString(PatchStringSerializer, Patch.Value("Fantasy"))

        assertEquals("\"Fantasy\"", result)
    }


    @Test
    /**
     * `encodeDefaults = true` is required here specifically to force `serialize()` to actually be called for the
     * `Undefined` field; with the app's real default (`false`), this path is normally unreachable.
    */
    fun `serializing a full wrapper with Undefined field throws`()
    {
        val json = Json { encodeDefaults = true }
        val wrapper = PatchTestWrapper(name = Patch.Value("Fantasy"))

        assertThrows<SerializationException> {
            json.encodeToString(wrapper)
        }
    }
}
