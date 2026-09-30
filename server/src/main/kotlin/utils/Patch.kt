package dev.jovanni0.utils



/**
 * tri-state wrapper for PATCH request fields: Undefined (field absent, don't
 * touch), Value(null) (explicit clear), Value(x) (update).
 *
 * Patch is intended for deserialization only. serializing it back out
 * requires the Json instance to have `encodeDefaults = true`, otherwise
 * fields left at `Patch.Undefined` are silently omitted before the
 * serializer is ever invoked.
 */
sealed class Patch<out TypeT>
{
    data object Undefined: Patch<Nothing>()
    data class Value<TypeT>(val value: TypeT?) : Patch<TypeT>()
}
