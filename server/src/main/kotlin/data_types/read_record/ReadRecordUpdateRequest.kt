package dev.jovanni0.data_types.read_record

import dev.jovanni0.utils.Update
import dev.jovanni0.utils.UpdateBooleanSerializer
import dev.jovanni0.utils.UpdateNullableLocalDateSerializer
import dev.jovanni0.utils.UpdateNullableStringSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class ReadRecordUpdateRequest(
    @Serializable(with = UpdateNullableStringSerializer::class)
    val edition_id:     Update<String?> = Update.Undefined,
    @Serializable(with = UpdateNullableLocalDateSerializer::class)
    val start_date:     Update<LocalDate?> = Update.Undefined,
    @Serializable(with = UpdateNullableLocalDateSerializer::class)
    val end_date:       Update<LocalDate?> = Update.Undefined,
    @Serializable(with = UpdateBooleanSerializer::class)
    val did_not_finish: Update<Boolean> = Update.Undefined,
    @Serializable(with = UpdateBooleanSerializer::class)
    val is_historical:  Update<Boolean> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val stop_position:  Update<String?> = Update.Undefined,
    @Serializable(with = UpdateNullableStringSerializer::class)
    val notes:          Update<String?> = Update.Undefined,
)
