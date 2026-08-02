package utils.patch

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.PatchStringSerializer
import kotlinx.serialization.Serializable



@Serializable
data class PatchTestWrapper(
    @Serializable(with = PatchStringSerializer::class)
    val name: Patch<String> = Patch.Undefined,

    @Serializable(with = PatchStringSerializer::class)
    val description: Patch<String> = Patch.Undefined,

    val untouched: String = "default",
)
