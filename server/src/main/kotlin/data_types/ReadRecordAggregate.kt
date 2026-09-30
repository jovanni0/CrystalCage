package dev.jovanni0.data_types

import java.time.LocalDate



data class ReadRecordAggregate(
    val end_date: LocalDate?,
    val dnf: Boolean,
    val is_historical: Boolean,
)
