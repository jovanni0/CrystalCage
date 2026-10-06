package dev.jovanni0.records.tags

import dev.jovanni0.data_types.tags.TagDetailResponse
import kotlin.uuid.Uuid



data class TagDetailRow(
    val id: Uuid,
    val category_id: Uuid?,
    val name: String,
    val description: String?,
    val books: List<TagBookRow>
)
{
    fun toTagDetailResponse(): TagDetailResponse
    {
        return TagDetailResponse(
            id = this.id.toString(),
            category_id = this.category_id?.toString(),
            name = this.name,
            description = this.description,
            books = this.books.map { it.toTagBookItem() },
        )
    }
}
