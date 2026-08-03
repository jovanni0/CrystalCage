package filesystem

import dev.jovanni0.exceptions.PathTooLongException
import dev.jovanni0.filesystem.AssetCategory
import dev.jovanni0.filesystem.BookFilenameContext
import dev.jovanni0.filesystem.FilenameBuilder
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.uuid.Uuid



class FilenameBuilderTest
{
    private val id = Uuid.random()


    private fun context(
        first: String? = "Neal",
        last: String = "Asher",
        universe: String? = "Polity",
        series: String? = "Agent Cormac",
        volume_number: Float? = 4f,
        title: String = "Polity Agent"
    ) = BookFilenameContext(first, last, universe, series, volume_number, title)


    @Test
    fun `builds full filename with all fields present`()
    {
        val name = FilenameBuilder.build(context(), AssetCategory.COVER, "front", id, "jpg")

        assertEquals(
            "Asher, Neal - Polity - Agent Cormac - 04 - Polity Agent - front.$id.jpg",
            name
        )
    }


    @Test
    fun `falls back to category name when label is null`()
    {
        val name = FilenameBuilder.build(context(), AssetCategory.COVER, null, id, "jpg")

        assertTrue(name.contains(" - cover.$id.jpg"))
    }


    @Test
    fun `drops series before universe when over byte budget`()
    {
        val long_series = "S".repeat(200)
        val name = FilenameBuilder.build(
            context(series = long_series), AssetCategory.EDITION, "epub", id, "epub"
        )

        assertTrue(!name.contains(long_series), "series should have been dropped")
        assertTrue(name.contains("Polity"), "universe should survive if series alone was over budget")
    }


    @Test
    fun `drops universe after series still over budget`()
    {
        val long_series = "S".repeat(200)
        val long_universe = "U".repeat(200)
        val name = FilenameBuilder.build(
            context(series = long_series, universe = long_universe), AssetCategory.EDITION, "epub", id, "epub"
        )

        assertTrue(!name.contains(long_series))
        assertTrue(!name.contains(long_universe))
        assertTrue(name.contains("Asher, Neal"), "author should survive at this stage")
    }


    @Test
    fun `drops author first name before touching title`()
    {
        val long_everything = "X".repeat(500)
        val name = FilenameBuilder.build(
            context(series = long_everything, universe = long_everything, title = "Polity Agent".repeat(16)),
            AssetCategory.EDITION, "epub", id, "epub"
        )

        assertTrue(name.startsWith("Asher -") || name.startsWith("Asher,").not())
        assertTrue(name.contains("Polity Agent"), "title should be untouched while author-first-name drop still fits")
    }


    @Test
    fun `truncates title as last resort but never below minimum length`()
    {
        val huge_title = "This Is An Extremely Long Book Title ".repeat(10)
        val name = FilenameBuilder.build(
            context(series = "S".repeat(100), universe = "U".repeat(100), title = huge_title),
            AssetCategory.EDITION, "epub", id, "epub"
        )
        val name_bytes = name.toByteArray().size

        assertTrue(name_bytes <= 255, "filename must respect the 255 byte cap, was $name_bytes")
    }


    @Test
    fun `throws when even minimum title length cannot fit budget`()
    {
        val huge_label = "L".repeat(300)

        assertFailsWith<PathTooLongException> {
            FilenameBuilder.build(context(), AssetCategory.COVER, huge_label, id, "jpg")
        }
    }


    @Test
    fun `throws when full path would exceed max path length`()
    {
        assertFailsWith<PathTooLongException> {
            FilenameBuilder.build(
                context(), AssetCategory.COVER, "front", id, "jpg",
                base_path_length = 250
            )
        }
    }


    @Test
    fun `omits null optional fields cleanly without stray separators`()
    {
        val name = FilenameBuilder.build(
            context(first = null, universe = null, series = null, volume_number = null),
            AssetCategory.COVER, null, id, "jpg"
        )

        assertEquals("Asher - Polity Agent - cover.$id.jpg", name)
    }
}
