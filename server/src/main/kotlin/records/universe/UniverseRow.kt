package dev.jovanni0.records.universe

import dev.jovanni0.data_types.universe.response.UniverseResponse
import kotlin.uuid.Uuid



data class UniverseRow(
    val id: Uuid,
    val author_id: Uuid,
    val name: String,
)
{
    fun toUniverseResponse(): UniverseResponse
    {
        return UniverseResponse(
            id = this.id.toString(),
            author_id = this.author_id.toString(),
            name = this.name,
        )
    }
}
