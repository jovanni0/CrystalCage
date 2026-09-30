package dev.jovanni0.records.series

import dev.jovanni0.data_types.series.SeriesResponse
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid



data class SeriesRow(
    val id: Uuid,
    val author_id: Uuid,
    val universe_id: Uuid?,
    val name: String,
    val is_finished: Boolean,
)
{
    fun toSeriesResponse(): SeriesResponse
    {
        return SeriesResponse(
            id = this.id.toString(),
            author_id = this.author_id.toString(),
            universe_id = this.universe_id?.toString(),
            name = this.name,
            is_finished = this.is_finished,
        )
    }
}
