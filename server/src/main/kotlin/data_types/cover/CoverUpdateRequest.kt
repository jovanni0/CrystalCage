package dev.jovanni0.data_types.cover

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.PatchStringSerializer
import kotlinx.serialization.Serializable



@Serializable
data class CoverUpdateRequest(
    @Serializable(with = PatchStringSerializer::class)
    val label: Patch<String> = Patch.Undefined
)
