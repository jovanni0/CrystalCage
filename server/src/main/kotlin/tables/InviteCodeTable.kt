package dev.jovanni0.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone


object InviteCodeTable: Table("invite_code")
{
    val code        = char("code", 8)
    val created_by  = uuid("created_by").nullable()
    val used_by     = uuid("used_by").uniqueIndex().nullable()
    val created_at  = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val expires_at  = timestampWithTimeZone("expires_at").nullable()
    val used_at     = timestampWithTimeZone("used_at").nullable()

    override val primaryKey = PrimaryKey(code)
}
