package dev.jovanni0.utils

import java.time.LocalDate



fun validateHistoricalTransaction(
    is_historical: Patch<Boolean>,
    current_is_historical: Boolean,
    effective_start_date: LocalDate?,
    effective_end_date: LocalDate?,
): String?
{
    if (is_historical is Patch.Value && is_historical.value == null)
    {
        return "is_historical cannot be null"
    }

    val ends_up_historical = when(is_historical)
    {
        is Patch.Undefined -> current_is_historical
        is Patch.Value -> is_historical.value!!
    }

    if (ends_up_historical && (effective_start_date != null || effective_end_date != null))
    {
        return "cannot mark record as historical while start_date or end_date is set"
    }

    if (!ends_up_historical && effective_start_date == null)
    {
        return "cannot mark record as non-historical while start_date is null"
    }

    return null
}
