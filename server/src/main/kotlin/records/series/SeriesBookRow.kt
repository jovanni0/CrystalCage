package dev.jovanni0.records.series

import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.data_types.series.SeriesBookItem
import java.time.LocalDate
import kotlin.uuid.Uuid


data class SeriesBookRow(
    val id:            Uuid,
    val cover_id:      Uuid?,
    val volume_number: Float?,
    val title:         String,
    val launch_date:   LocalDate?,
    val word_count:    Int?,
    val read_status:   ReadStatus,
    val reread_count:  Int,
)
{
    fun toSeriesBookItem(): SeriesBookItem
    {
        return SeriesBookItem(
            id = this.id.toString(),
            cover_id = this.cover_id?.toString(),
            volume_number = this.volume_number,
            title = this.title,
            launch_date = this.launch_date,
            word_count = this.word_count,
            read_status = this.read_status,
            reread_count = this.reread_count,
        )
    }
}
