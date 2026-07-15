package dev.jovanni0.data_types.universe.request

import kotlinx.serialization.Serializable



@Serializable
data class UniverseCreateRequest(
    val author_id: String,
    val name: String,
) {
    fun validate(): String?
    {
        if (name.isBlank())
        {
            return "name cannot be blank"
        }

        return null
    }
}
