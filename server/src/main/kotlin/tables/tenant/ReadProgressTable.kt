package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone


object ReadProgressTable: Table("read_progress")
{
    val id = uuid("id").autoGenerate()
    val read_record_id = uuid("read_record_id").references(ReadRecordTable.id, onDelete = ReferenceOption.CASCADE)
    val recorded_at = timestampWithTimeZone("recorded_at")
    val progress = decimal("progress", 5, 2)

    override val primaryKey = PrimaryKey(id)
}
