package dev.jovanni0.data_types.book

import dev.jovanni0.utils.Update
import dev.jovanni0.utils.UpdateLocalDateSerializer
import dev.jovanni0.utils.UpdateNullableFloatSerializer
import dev.jovanni0.utils.UpdateNullableIntSerializer
import dev.jovanni0.utils.UpdateNullableLocalDateSerializer
import dev.jovanni0.utils.UpdateNullableStringSerializer
import dev.jovanni0.utils.UpdateStringSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class BookUpdateRequest(
    @Serializable(with = UpdateStringSerializer::class)
    val author_id: Update<String> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val universe_id: Update<String?> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val series_id: Update<String?> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val cover_id: Update<String?> = Update.Undefined,
    @Serializable(with = UpdateNullableFloatSerializer::class)
    val volume_number: Update<Float?> = Update.Undefined,
    @Serializable(with = UpdateStringSerializer::class)
    val title: Update<String> = Update.Undefined,
    @Serializable(with = UpdateNullableLocalDateSerializer::class)
    val launch_date: Update<LocalDate?> = Update.Undefined,
    @Serializable(with = UpdateNullableIntSerializer::class)
    val word_count: Update<Int?> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val blurb: Update<String?> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val notes: Update<String?> = Update.Undefined,
    @Serializable(with = UpdateStringSerializer::class)
    val tag_ids: Update<List<String>> = Update.Undefined,
)
