package dev.jovanni0.filesystem

import dev.jovanni0.exceptions.AssetStorageException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.uuid.Uuid



class AssetStorage(private val base_directory: Path)
{
    fun store(
        schema_name: String,
        category: AssetCategory,
        filename: String,
        data: InputStream
    ): Path
    {
        val dir_path = resolveDirectory(schema_name, category)
        Files.createDirectories(dir_path)

        val file_path = dir_path.resolve(filename)
        val temp_file_path = Files.createTempFile(dir_path, "tmp-", ".part")

        if (Files.exists(file_path))
        {
            throw AssetStorageException("refusing to overwrite existing asset at $file_path. this indicates an ID collision or leftover file from a prior failed write. investigate before retrying.")
        }

        try {
            data.use {
                Files.copy(it, temp_file_path, StandardCopyOption.REPLACE_EXISTING)
            }

            Files.move(temp_file_path, file_path, StandardCopyOption.ATOMIC_MOVE)

            return file_path
        }
        catch (e: Exception) {
            Files.deleteIfExists(temp_file_path)

            throw AssetStorageException("failed to store asset at $file_path", e)
        }
    }


    /**
     * delete a file from inside the dir specified by the user schema and category.
     */
    fun delete(schema_name: String, category: AssetCategory, filename: String): Boolean
    {
        val file_path = resolveDirectory(schema_name, category).resolve(filename)

        return try {
            Files.deleteIfExists(file_path)
        }
        catch (e: Exception)
        {
            throw AssetStorageException("failed to delete asset at $file_path", e)
        }
    }


    /**
     * list all files in the dir specified by the user schema and category.
     */
    fun listFiles(schema_name: String, category: AssetCategory): List<Path>
    {
        val dir = resolveDirectory(schema_name, category)

        if (!Files.exists(dir))
        {
            return emptyList()
        }

        return Files.list(dir).use { it.toList() }
    }


    /**
     * resolve the destination path for the asset, composed as `base_path/schema_name/asset_category`.
     */
    fun resolveDirectory(schema_name: String, category: AssetCategory): Path
    {
        return base_directory.resolve(schema_name).resolve(category.dir_name)
    }


    fun findByEmbeddedId(schema_name: String, category: AssetCategory, id: Uuid): Path?
    {
        val dir_path = resolveDirectory(schema_name, category)

        if (!Files.exists(dir_path))
        {
            return null
        }

        val match = Files
            .list(dir_path)
            .use { stream ->
                stream.filter { path ->
                    val name = path.fileName.toString()
                    val without_extension = name.substringBeforeLast('.')

                    without_extension.substringAfterLast('.') == id.toString()
                }
            }
            .findFirst()
            .orElse(null)

        return match
    }
}
