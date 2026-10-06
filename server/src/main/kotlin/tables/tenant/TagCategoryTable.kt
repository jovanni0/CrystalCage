package dev.jovanni0.tables.tenant

import org.jetbrains.exposed.v1.core.Table



object TagCategoryTable : Table("tag_category")
{
    val id = uuid("id").autoGenerate()
    val name = varchar("`name`", 255).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
