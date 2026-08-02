package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.Table



object TagTable: Table("tag")
{
    val id = uuid("id").autoGenerate()
    val name = varchar("name", 255).uniqueIndex()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}
