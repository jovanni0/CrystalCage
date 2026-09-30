package dev.jovanni0.data_types.edition

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.PatchStringSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class EditionUpdateRequest(
    @Serializable(with = PatchStringSerializer::class)
    val label: Patch<String> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val release_date: Patch<LocalDate> = Patch.Undefined,
    @Serializable(with = PatchStringSerializer::class)
    val format: Patch<String> = Patch.Undefined,
)
