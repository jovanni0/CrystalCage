package dev.jovanni0.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import tools.jackson.databind.ser.jdk.BooleanSerializer
import java.time.LocalDate



object UpdateStringSerializer : KSerializer<Update<String>> by UpdateSerializer(String.serializer())
object UpdateNullableStringSerializer : KSerializer<Update<String?>> by UpdateSerializer(String.serializer().nullable)

object UpdateIntSerializer : KSerializer<Update<Int>> by UpdateSerializer(Int.serializer())
object UpdateNullableIntSerializer : KSerializer<Update<Int?>> by UpdateSerializer(Int.serializer().nullable)

object UpdateFloatSerializer : KSerializer<Update<Float>> by UpdateSerializer(Float.serializer())
object UpdateNullableFloatSerializer : KSerializer<Update<Float?>> by UpdateSerializer(Float.serializer().nullable)

object UpdateLocalDateSerializer : KSerializer<Update<LocalDate>> by UpdateSerializer(LocalDateSerializer)
object UpdateNullableLocalDateSerializer : KSerializer<Update<LocalDate?>> by UpdateSerializer(LocalDateSerializer.nullable)

object UpdateBooleanSerializer : KSerializer<Update<Boolean>> by UpdateSerializer(Boolean.serializer())
object UpdateNullableBooleanSerializer : KSerializer<Update<Boolean?>> by UpdateSerializer(Boolean.serializer().nullable)


class UpdateSerializer<TypeT>(private val value_serializer: KSerializer<TypeT>) : KSerializer<Update<TypeT>>
{
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Update", value_serializer.descriptor)


    override fun deserialize(decoder: Decoder): Update<TypeT>
    {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return Update.Value(decoder.decodeSerializableValue(value_serializer))

        val element = jsonDecoder.decodeJsonElement()

        return Update.Value(jsonDecoder.json.decodeFromJsonElement(value_serializer, element))
    }


    override fun serialize(encoder: Encoder, value: Update<TypeT>)
    {
        when (value)
        {
            is Update.Undefined -> throw SerializationException("Update.Undefined should never be serialized")
            is Update.Value -> encoder.encodeSerializableValue(value_serializer, value.value)
        }
    }
}
