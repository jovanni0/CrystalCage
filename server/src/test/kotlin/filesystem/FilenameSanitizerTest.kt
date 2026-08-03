package filesystem

import dev.jovanni0.filesystem.FilenameSanitizer
import kotlin.test.Test
import kotlin.test.assertEquals



class FilenameSanitizerTest
{
    @Test
    fun `removes forbidden filesystem characters`()
    {
        val result = FilenameSanitizer.sanitize("Title: The <Return> of \"Chaos\"?")

        assertEquals("Title The Return of Chaos", result)
    }


    @Test
    fun `transliterates accented characters to ascii`()
    {
        assertEquals("Zizek", FilenameSanitizer.sanitize("Žižek"))
        assertEquals("cafe", FilenameSanitizer.sanitize("café"))
    }


    @Test
    fun `replaces non-latin scripts with underscore per character`()
    {
        val result = FilenameSanitizer.sanitize("日本語")
        assertEquals("___", result)
    }


    @Test
    fun `collapses repeated whitespace and trims`()
    {
        assertEquals("A B", FilenameSanitizer.sanitize("  A    B  "))
    }


    @Test
    fun `leaves already-clean ascii untouched`()
    {
        assertEquals("Book Title 2024", FilenameSanitizer.sanitize("Book Title 2024"))
    }
}
