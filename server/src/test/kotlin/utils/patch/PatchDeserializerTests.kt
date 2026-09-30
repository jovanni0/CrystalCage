package utils.patch

import dev.jovanni0.utils.Patch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals



class PatchSerializerTest
{
    @Test
    fun `absent field decodes to Undefined`()
    {
        val result = Json.decodeFromString<PatchTestWrapper>("""{}""")

        assertEquals(Patch.Undefined, result.name)
        assertEquals(Patch.Undefined, result.description)
    }


    @Test
    fun `explicit null decodes to Value(null)`()
    {
        val result = Json.decodeFromString<PatchTestWrapper>("""{"name": null}""")

        assertEquals(Patch.Value(null), result.name)
    }


    @Test
    fun `real value decodes to Value holding that value`()
    {
        val result = Json.decodeFromString<PatchTestWrapper>("""{"name": "Fantasy"}""")

        assertEquals(Patch.Value("Fantasy"), result.name)
    }


    @Test
    fun `mixed presence resolves each field independently`()
    {
        val result = Json.decodeFromString<PatchTestWrapper>(
            """{"name": "Fantasy", "description": null}"""
        )

        assertEquals(Patch.Value("Fantasy"), result.name)
        assertEquals(Patch.Value(null), result.description)
    }


    @Test
    fun `both fields absent leaves both Undefined and non-patch field on its default`()
    {
        val result = Json.decodeFromString<PatchTestWrapper>("""{}""")

        assertEquals(Patch.Undefined, result.name)
        assertEquals(Patch.Undefined, result.description)
        assertEquals("default", result.untouched)
    }


    @Test
    fun `non-patch field alongside patch fields decodes independently`()
    {
        val result = Json.decodeFromString<PatchTestWrapper>(
            """{"name": "Fantasy", "untouched": "custom"}"""
        )

        assertEquals(Patch.Value("Fantasy"), result.name)
        assertEquals(Patch.Undefined, result.description)
        assertEquals("custom", result.untouched)
    }


    @Test
    fun `empty JSON object does not throw when all fields have defaults`()
    {
        assertDoesNotThrow {
            Json.decodeFromString<PatchTestWrapper>("""{}""")
        }
    }

    @Test
    fun `wrong JSON type for the field throws`()
    {
        assertThrows<SerializationException> {
            Json.decodeFromString<PatchTestWrapper>("""{"name": 123}""")
        }
    }

    @Test
    fun `malformed JSON throws`()
    {
        assertThrows<SerializationException> {
            Json.decodeFromString<PatchTestWrapper>("""{"name": """)
        }
    }
}
