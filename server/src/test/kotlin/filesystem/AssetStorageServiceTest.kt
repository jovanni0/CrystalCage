package filesystem

import dev.jovanni0.exceptions.AssetStorageException
import dev.jovanni0.filesystem.AssetCategory
import dev.jovanni0.filesystem.AssetStorage
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue



class AssetStorageServiceTest
{
    @TempDir
    lateinit var temp_dir: Path

    
    private fun assetStorage() = AssetStorage(temp_dir)

    
    
    @Test
    fun `store creates user and category directories if missing`() 
    {
        val service = assetStorage()
        val data = "fake image bytes".byteInputStream()

        val result = service.store("user_42", AssetCategory.COVER, "book.uuid.jpg", data)

        assertTrue(Files.exists(result))
        assertEquals(temp_dir.resolve("user_42/covers/book.uuid.jpg"), result)
    }

    
    @Test
    fun `store writes the exact bytes provided`() 
    {
        val service = assetStorage()
        val content = "hello world".toByteArray()

        val result = service.store("user_1", AssetCategory.EDITION, "book.uuid.epub", ByteArrayInputStream(content))

        assertContentEquals(content, Files.readAllBytes(result))
    }

    
    @Test
    fun `store does not leave a temp file behind on success`() 
    {
        val service = assetStorage()
        
        service.store("user_1", AssetCategory.COVER, "book.uuid.jpg", "data".byteInputStream())

        val leftover_temp_files = Files
            .list(temp_dir.resolve("user_1/covers"))
            .use {
                it
                    .filter { p -> p.toString().contains("tmp-") }
                    .toList()
            }

        assertTrue(leftover_temp_files.isEmpty())
    }

    
    @Test
    fun `store cleans up temp file when the input stream fails mid-read`()
    {
        val service = assetStorage()
        val failing_stream = object : InputStream()
        {
            override fun read(): Int = throw IOException("simulated disk failure")
        }

        assertFailsWith<AssetStorageException> {
            service.store("user_1", AssetCategory.COVER, "book.uuid.jpg", failing_stream)
        }

        val dir = temp_dir.resolve("user_1/covers")
        val leftovers = if (Files.exists(dir)) Files.list(dir).use { it.toList() } else emptyList()

        assertTrue(leftovers.isEmpty(), "expected no files left after a failed write, found: $leftovers")
    }


    @Test
    fun `store overwrites an existing file at the same path`()
    {
        val service = assetStorage()

        service.store("user_1", AssetCategory.COVER, "book.uuid.jpg", "old".byteInputStream())

        assertFailsWith<AssetStorageException> {
            service.store("user_1", AssetCategory.COVER, "book.uuid.jpg", "new".byteInputStream())
        }
        assertEquals("old", Files.readString(temp_dir.resolve("user_1/covers/book.uuid.jpg")))
    }


    @Test
    fun `delete removes an existing file and returns true`()
    {
        val service = assetStorage()

        service.store("user_1", AssetCategory.EDITION, "book.uuid.epub", "data".byteInputStream())

        val deleted = service.delete("user_1", AssetCategory.EDITION, "book.uuid.epub")

        assertTrue(deleted)
        assertFalse(Files.exists(temp_dir.resolve("user_1/editions/book.uuid.epub")))
    }


    @Test
    fun `delete returns false when file does not exist`()
    {
        val service = assetStorage()
        val deleted = service.delete("user_1", AssetCategory.EDITION, "nonexistent.uuid.epub")

        assertFalse(deleted)
    }


    @Test
    fun `listFiles returns empty list when category directory was never created`()
    {
        val service = assetStorage()

        assertEquals(emptyList(), service.listFiles("user_1", AssetCategory.COVER))
    }


    @Test
    fun `listFiles reflects files written for that user and category only`()
    {
        val service = assetStorage()

        service.store("user_1", AssetCategory.COVER, "a.uuid.jpg", "x".byteInputStream())
        service.store("user_1", AssetCategory.EDITION, "b.uuid.epub", "x".byteInputStream())
        service.store("2", AssetCategory.COVER, "c.uuid.jpg", "x".byteInputStream())

        val covers = service.listFiles("user_1", AssetCategory.COVER)

        assertEquals(1, covers.size)
        assertEquals("a.uuid.jpg", covers.first().fileName.toString())
    }


    @Test
    fun `resolveDirectory keeps users isolated from each other`()
    {
        val service = assetStorage()

        assertNotEquals(
            service.resolveDirectory("user_1", AssetCategory.COVER),
            service.resolveDirectory("2", AssetCategory.COVER)
        )
    }
}
