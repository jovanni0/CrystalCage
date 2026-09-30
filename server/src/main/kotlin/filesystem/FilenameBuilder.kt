package dev.jovanni0.filesystem

import dev.jovanni0.exceptions.PathTooLongException
import kotlin.uuid.Uuid



object FilenameBuilder
{
    private const val MAX_FILENAME_BYTES = 255
    private const val MIN_TITLE_LENGTH = 8


    /**
     * @param base_path_length  length of the directory the file will be written to, used to validate the final path will
     * fit in the `max_path_length` size limit.
     */
    fun build(
        context: BookFilenameContext,
        category: AssetCategory,
        label: String?,
        id: Uuid,
        extension: String,
        base_path_length: Int = 0,
        max_path_length: Int = 260
    ): String
    {
        val suffix = ".$id.${extension.trimStart('.')}"
        val remaining_size_budget = MAX_FILENAME_BYTES - suffix.toByteArray().size

        var author = formatAuthor(context.first_name, context.last_name)
        var universe = context.universe
        var series = context.series
        val volume_number = context.volume_number
            ?.toBigDecimal()
            ?.stripTrailingZeros()
            ?.toPlainString()
            ?.padStart(2, '0')
        var title = context.title
        val asset_label = label ?: category.dir_name.dropLast(1)

        fun assamble(): String
        {
            val parts = listOfNotNull(author, universe, series, volume_number, title, asset_label)
            val name = parts.joinToString(" - ")

            return FilenameSanitizer.sanitize(name)
        }

        var name = assamble()
        if (name.toByteArray().size > remaining_size_budget)
        {
            series = null
            name = assamble()
        }
        if (name.toByteArray().size > remaining_size_budget)
        {
            universe = null
            name = assamble()
        }
        if (name.toByteArray().size > remaining_size_budget)
        {
            author = formatAuthor(null, context.last_name)
            name = assamble()
        }

        while (name.toByteArray().size > remaining_size_budget && title.length > MIN_TITLE_LENGTH)
        {
            title = title.dropLast(1).trim()
            name = assamble()
        }

        if (name.toByteArray().size > remaining_size_budget)
        {
            throw PathTooLongException("could not build a filename for asset $id within the $MAX_FILENAME_BYTES-bytes limit")
        }

        val filename = "$name$suffix"

        if (base_path_length + filename.length > max_path_length)
        {
            throw PathTooLongException("full path for asset $id would exceed $max_path_length characters; use a shorter base directory.")
        }

        return filename
    }


    private fun formatAuthor(first_name: String?, last_name: String): String
    {
        if (first_name == null)
        {
            return last_name
        }

        return "$last_name, $first_name"
    }
}
