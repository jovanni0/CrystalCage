package dev.jovanni0.utils

import dev.jovanni0.tables.tenant.AuthorTable
import dev.jovanni0.tables.tenant.UniverseTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import kotlin.uuid.Uuid



fun authorExists(author_id: Uuid): Boolean
{
    return AuthorTable
        .selectAll()
        .where { AuthorTable.id eq author_id }
        .empty()
        .not()
}



fun universeExists(universe_id: Uuid): Boolean
{
    return UniverseTable
        .selectAll()
        .where { UniverseTable.id eq universe_id }
        .empty()
        .not()
}
