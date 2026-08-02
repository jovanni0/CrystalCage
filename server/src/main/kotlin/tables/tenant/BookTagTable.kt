package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table



object BookTagTable: Table("book_tag")
{
    val book_id = uuid("book_id").references(BookTable.id, onDelete = ReferenceOption.CASCADE)
    val tag_id = uuid("tag_id").references(BookTable.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(book_id, tag_id)
}
