package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.Table



object AuthorTable: Table("author")
{
    val id = uuid("id").autoGenerate()
    val first_name = varchar("first_name", 255).nullable()
    val last_name = varchar("last_name", 255)

    override val primaryKey = PrimaryKey(id)
}
