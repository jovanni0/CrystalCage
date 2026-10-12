package dev.jovanni0.filesystem

import dev.jovanni0.exceptions.AssetStorageException
import dev.jovanni0.utils.CoverSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import net.coobird.thumbnailator.Thumbnails
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import kotlin.uuid.Uuid


class CoverThumbnails(private val base_directory: Path)
{
    private val generation_permits = Semaphore(2)


    fun pathOf(schema_name: String, cover_id: Uuid, size: CoverSize): Path
    {
        return base_directory
            .resolve(schema_name)
            .resolve(".thumbnails")
            .resolve("$cover_id-${size.query_value}.jpg")
    }


    suspend fun ensure(schema_name: String, cover_id: Uuid, original: Path, size: CoverSize): Path
    {
        val target = pathOf(schema_name, cover_id, size)

        if (Files.exists(target))
        {
            return target
        }

        generation_permits.withPermit {
            if (!Files.exists(target))
            {
                withContext(Dispatchers.IO)
                {
                    generate(original, target, size.max_width!!)
                }
            }
        }

        return target
    }


    fun deleteAll(schema_name: String, cover_id: Uuid)
    {
        CoverSize.entries
            .filter { it.max_width != null }
            .forEach { Files.deleteIfExists(pathOf(schema_name, cover_id, it)) }
    }


    private fun generate(original: Path, target: Path, max_width: Int)
    {
        Files.createDirectories(target.parent)

        val temp_file = Files.createTempFile(target.parent, "tmp-", ".part")

        try
        {
            val source = ImageIO.read(original.toFile()) ?: throw AssetStorageException("cannot decode image at $original")

            Files.newOutputStream(temp_file).use {
                Thumbnails
                    .of(source)
                    .width(minOf(max_width, source.width))
                    .imageType(BufferedImage.TYPE_INT_RGB)
                    .outputFormat("jpg")
                    .outputQuality(0.8)
                    .toOutputStream(it)
            }

            Files.move(temp_file, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        }
        finally
        {
            Files.deleteIfExists(temp_file)
        }
    }
}