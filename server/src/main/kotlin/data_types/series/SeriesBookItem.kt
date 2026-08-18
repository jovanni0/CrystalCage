package dev.jovanni0.data_types.series

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate


@Serializable
data class SeriesBookItem(
    val id:            String,
    val cover_id:      String?,
    val volume_number: Float?,
    val title:         String,
    @Serializable(with = LocalDateSerializer::class)
    val launch_date:   LocalDate?,
    val word_count:    Int?,
    val read_status:   ReadStatus,
    val reread_count:  Int,
)
