package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date



object BookEditionTable: Table("book_edition")
{
    val id = uuid("id").autoGenerate()
    val book_id = uuid("book_id").references(BookTable.id, onDelete = ReferenceOption.CASCADE)
    val label = varchar("label", 255).nullable()
    val release_date = date("release_date").nullable()
    val added_at = date("added_at")
    val format = varchar("format", 255)

    override val primaryKey = PrimaryKey(id)
}
