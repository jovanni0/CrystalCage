package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table



object SeriesTable: Table("series")
{
    val id = uuid("id").autoGenerate()
    val author_id = uuid("author_id").references(AuthorTable.id, onDelete = ReferenceOption.CASCADE)
    val universe_id = uuid("universe_id").references(UniverseTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val name = varchar("name", 255)
    val is_finished = bool("is_finished").default(false)

    override val primaryKey = PrimaryKey(id)
}
