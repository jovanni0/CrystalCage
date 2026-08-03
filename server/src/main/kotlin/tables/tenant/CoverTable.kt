package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import java.time.LocalDate



object CoverTable: Table("cover")
{
    val id = uuid("id").autoGenerate()
    val book_id = uuid("book_id").references(BookTable.id, onDelete = ReferenceOption.CASCADE)
    val label = varchar("label", 255).nullable()
    val added_at = date("added_at").default(LocalDate.now())

    override val primaryKey = PrimaryKey(id)
}
