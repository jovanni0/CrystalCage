package dev.jovanni0.tables

import org.jetbrains.exposed.v1.core.Table



object Author: Table("author")
{
    val id = uuid("id").autoGenerate()
    val first_name = varchar("first_name", 255).nullable()
    val last_name = varchar("last_name", 255)

    override val primaryKey = PrimaryKey(id)
}
