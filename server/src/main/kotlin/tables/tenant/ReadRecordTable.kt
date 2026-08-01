package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date



object ReadRecordTable: Table("read_record")
{
    val id = uuid("id").autoGenerate()
    val book_id = uuid("book_id").references(BookTable.id, onDelete = ReferenceOption.CASCADE)
    val edition_id = uuid("edition_id").references(BookEditionTable.id, onDelete = ReferenceOption.CASCADE).nullable()
    val start_date = date("start_date").nullable()
    val end_date = date("end_date").nullable()
    val did_not_finish = bool("did_not_finish").default(false)
    val is_historical = bool("is_historical").default(false)
    val stop_position = varchar("stop_position", 255).nullable()
    val notes = text("notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
