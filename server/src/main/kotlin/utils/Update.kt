package dev.jovanni0.utils

import java.math.BigDecimal



/**
 * replacement for Patch, that allows more control over the nullability of the value
 */
sealed class Update<out TypeT>
{
    data object Undefined: Update<Nothing>()
    data class Value<TypeT>(val value: TypeT) : Update<TypeT>()
}



fun Update<Float?>.toBidDecimal(): Update<BigDecimal?>
{
    return when (this)
    {
        is Update.Undefined -> Update.Undefined
        is Update.Value -> Update.Value(this.value?.let { BigDecimal.valueOf(it.toDouble()) })
    }
}
