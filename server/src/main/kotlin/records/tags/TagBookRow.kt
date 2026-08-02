package dev.jovanni0.records.tags

import dev.jovanni0.data_types.tags.TagBookItem
import kotlin.uuid.Uuid



data class TagBookRow(
    val id: Uuid,
    val title: String,
    val series_id: Uuid?,
    val cover_id: Uuid?,
) {
    fun toTagBookItem(): TagBookItem
    {
        return TagBookItem(
            id = this.id.toString(),
            title = this.title,
            series_id = this.series_id?.toString(),
            cover_id = this.cover_id?.toString(),
        )
    }
}
