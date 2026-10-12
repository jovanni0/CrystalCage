package dev.jovanni0.records

import dev.jovanni0.data_types.ReadProgressResponse
import java.math.BigDecimal
import java.time.OffsetDateTime
import kotlin.uuid.Uuid


data class ReadProgressRecord(
    val id: Uuid,
    val read_record_id: Uuid,
    val recorded_at: OffsetDateTime,
    val progress: BigDecimal
)
{
    fun toReadProgressResponse(): ReadProgressResponse
    {
        return ReadProgressResponse(
            id = this.id.toString(),
            read_record_id = this.read_record_id.toString(),
            recorded_at = this.recorded_at.toString(),
            progress = this.progress.toDouble()
        )
    }
}