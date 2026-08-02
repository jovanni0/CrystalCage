package dev.jovanni0.data_types.read_record

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.PatchStringSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class ReadRecordUpdateRequest(
    @Serializable(with = PatchStringSerializer::class)
    val edition_id: Patch<String> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val start_date: Patch<LocalDate> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val end_date: Patch<LocalDate> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val did_not_finish: Patch<Boolean> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val is_historical: Patch<Boolean> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val stop_position: Patch<String> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val notes: Patch<String> = Patch.Undefined,
)
