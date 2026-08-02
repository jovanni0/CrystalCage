package dev.jovanni0.data_types.read_record

import dev.jovanni0.utils.LocalDateSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate



@Serializable
data class ReadRecordCreateRequest(
    val book_id: String,
    val edition_id: String? = null,
    @Serializable(with = LocalDateSerializer::class)
    val start_date: LocalDate? = null,
    @Serializable(with = LocalDateSerializer::class)
    val end_date: LocalDate? = null,
    val did_not_finish: Boolean,
    val is_historical: Boolean,
    val stop_position: String? = null,
    val notes: String? = null,
) {
    fun validateDateLogic(): String?
    {
        if (is_historical && (start_date != null || end_date != null))
        {
            return "historical records cannot have start_date or end_date set"
        }

        if (!is_historical && start_date == null)
        {
            return "start_date is required for non-historical records"
        }

        return null
    }
}
