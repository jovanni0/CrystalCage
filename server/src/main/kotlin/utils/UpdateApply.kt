package dev.jovanni0.utils

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.statements.UpdateStatement



@JvmName("updateIfPresentNullable")
fun <TypeT : Any> UpdateStatement.updateIfPresent(column: Column<TypeT?>, updaete: Update<TypeT?>)
{
    if (updaete is Update.Value)
    {
        this[column] = updaete.value
    }
}



@JvmName("updateIfPresentNonNull")
fun <TypeT : Any> UpdateStatement.updateIfPresent(column: Column<TypeT>, update: Update<TypeT>)
{
    if (update is Update.Value)
    {
        this[column] = update.value
    }
}
