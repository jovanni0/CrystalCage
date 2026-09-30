package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date



object BookTable: Table("book")
{
    val id = uuid("id").autoGenerate()
    val author_id = uuid("author_id").references(AuthorTable.id, onDelete = ReferenceOption.CASCADE)
    val universe_id = uuid("universe_id").references(UniverseTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val series_id = uuid("series_id").references(SeriesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val cover_id = uuid("cover_id").nullable()
    val volume_number = decimal("volume_number", 3, 1).nullable()
    val title = varchar("title", 255)
    val launch_date = date("launch_date").nullable()
    val word_count = integer("word_count").nullable()
    val blurb = text("blurb").nullable()
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
