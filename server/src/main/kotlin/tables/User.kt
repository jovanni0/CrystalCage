package dev.jovanni0.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.CurrentDate
import org.jetbrains.exposed.v1.javatime.date



object User: Table("user")
{
    val id = uuid("id").autoGenerate()
    val username = varchar("username", 255).uniqueIndex()
    val password_hash = varchar("password_hash", 64)
    val role = varchar("role", 255)
    val created_at = date("created_at").defaultExpression(CurrentDate)

    override val primaryKey = PrimaryKey(id)
}
