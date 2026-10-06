package dev.jovanni0.records

import dev.jovanni0.data_types.TagCategoryResponse
import kotlin.uuid.Uuid



data class TagCategoryRecord(
    val id: Uuid,
    val name: String,
)
{
    fun toTagCategoryResponse(): TagCategoryResponse
    {
        return TagCategoryResponse(
            id = this.id.toString(),
            name = this.name
        )
    }
}