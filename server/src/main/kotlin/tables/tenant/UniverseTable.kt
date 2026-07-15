package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table



object UniverseTable: Table("universe")
{
    val id = uuid("id").autoGenerate()
    val author_id = uuid("author_id").references(AuthorTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(id)
}
