package dev.jovanni0.records.author

import dev.jovanni0.data_types.author.AuthorResponse
import kotlin.uuid.Uuid



data class AuthorRow(
    val id: Uuid,
    val first_name: String?,
    val last_name: String
)
{
    fun toAuthorResponse(): AuthorResponse
    {
        return AuthorResponse(
            id = this.id.toString(),
            first_name = this.first_name,
            last_name = this.last_name,
        )
    }
}
