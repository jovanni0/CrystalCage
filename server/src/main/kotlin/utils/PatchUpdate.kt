package dev.jovanni0.utils

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.statements.UpdateStatement



@JvmName("setIfPresentNullable")
fun <TypeT : Any> UpdateStatement.setIfPresent(column: Column<TypeT?>, patch: Patch<TypeT>)
{
    if (patch is Patch.Value)
    {
        this[column] = patch.value
    }
}



@JvmName("setIfPresentNonNull")
fun <TypeT : Any> UpdateStatement.setIfPresent(column: Column<TypeT>, patch: Patch<TypeT>)
{
    if (patch is Patch.Value)
    {
        val value = patch.value ?: throw IllegalArgumentException("${column.name} cannot be set to null")
        this[column] = value
    }
}
