package dev.jovanni0.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull



object PatchStringSerializer : KSerializer<Patch<String>> by PatchSerializer(String.serializer())



class PatchSerializer<TyperT : Any>(private val valueSerializer: KSerializer<TyperT>) : KSerializer<Patch<TyperT>>
{
    override val descriptor: SerialDescriptor = valueSerializer.descriptor


    override fun deserialize(decoder: Decoder): Patch<TyperT>
    {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return Patch.Value(decoder.decodeSerializableValue(valueSerializer))

        val element = jsonDecoder.decodeJsonElement()

        return if (element is JsonNull)
        {
            Patch.Value(null)
        }
        else
        {
            Patch.Value(jsonDecoder.json.decodeFromJsonElement(valueSerializer, element))
        }
    }


    @kotlinx.serialization.ExperimentalSerializationApi
    override fun serialize(encoder: Encoder, value: Patch<TyperT>)
    {
        when (value)
        {
            is Patch.Undefined ->
                // only reachable if the active Json instance has `encodeDefaults = true`.
                // with the default (false), `kotlinx.serialization` skips calling this
                // serializer entirely for properties still at their default value
                // (`Patch.Undefined`), so this throw is a defensive guardrail, not
                // something exercised under normal app configuration.
                throw SerializationException("Patch.Undefined should never be serialized")
            is Patch.Value ->
            {
                if (value.value == null)
                {
                    encoder.encodeNull()
                }
                else
                {
                    encoder.encodeSerializableValue(valueSerializer, value.value)
                }
            }
        }
    }
}
