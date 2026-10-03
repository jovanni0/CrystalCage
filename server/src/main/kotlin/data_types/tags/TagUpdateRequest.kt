package dev.jovanni0.data_types.tags

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.PatchStringSerializer
import dev.jovanni0.utils.Update
import dev.jovanni0.utils.UpdateNullableStringSerializer
import dev.jovanni0.utils.UpdateSerializer
import kotlinx.serialization.Serializable



@Serializable
data class TagUpdateRequest(
    @Serializable(with = PatchStringSerializer::class)
    val name: Patch<String> = Patch.Undefined,

    @Serializable(with = UpdateNullableStringSerializer::class)
    val category_id: Update<String?> = Update.Undefined,

    @Serializable(with = PatchStringSerializer::class)
    val description: Patch<String> = Patch.Undefined,

    val added_book_ids: List<String> = listOf(),
    val removed_book_ids: List<String> = listOf(),
)
