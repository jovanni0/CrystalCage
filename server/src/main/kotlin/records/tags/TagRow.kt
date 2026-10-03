package dev.jovanni0.records.tags

import dev.jovanni0.data_types.tags.TagResponse
import kotlin.uuid.Uuid



data class TagRow(
    val id: Uuid,
    val category_id: Uuid?,
    val name: String,
    val description: String?,
)
{
    fun toTagResponse(book_count: Int = 0): TagResponse
    {
        return TagResponse(
            id = this.id.toString(),
            category_id = this.category_id?.toString(),
            name = name,
            description = description,
            book_count = book_count
        )
    }
}
