package utils

import dev.jovanni0.utils.Patch
import dev.jovanni0.utils.validateHistoricalTransaction
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull



class ValidateHistoricalTransactionTests
{
    @Test
    fun `explicitly becoming historical with both dates null returns no error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(true),
            current_is_historical = false,
            effective_start_date = null,
            effective_end_date = null,
        )

        assertNull(result)
    }


    @Test
    fun `explicitly becoming historical with start_date set returns error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(true),
            current_is_historical = false,
            effective_start_date = LocalDate.now(),
            effective_end_date = null,
        )

        assertNotNull(result)
    }


    @Test
    fun `explicitly becoming historical with end_date set returns error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(true),
            current_is_historical = false,
            effective_start_date = null,
            effective_end_date = LocalDate.now(),
        )

        assertNotNull(result)
    }


    @Test
    fun `explicitly becoming historical with both dates set returns error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(true),
            current_is_historical = false,
            effective_start_date = LocalDate.now(),
            effective_end_date = LocalDate.now(),
        )

        assertNotNull(result)
    }


    @Test
    fun `staying historical implicitly with a date patched in returns error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Undefined,
            current_is_historical = true,
            effective_start_date = LocalDate.now(),
            effective_end_date = null,
        )

        assertNotNull(result)
    }


    @Test
    fun `staying historical implicitly with dates still null returns no error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Undefined,
            current_is_historical = true,
            effective_start_date = null,
            effective_end_date = null,
        )

        assertNull(result)
    }


    @Test
    fun `explicitly becoming non-historical with start_date set returns no error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(false),
            current_is_historical = true,
            effective_start_date = LocalDate.now(),
            effective_end_date = null,
        )

        assertNull(result)
    }


    @Test
    fun `explicitly becoming non-historical with start_date null returns error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(false),
            current_is_historical = true,
            effective_start_date = null,
            effective_end_date = null,
        )

        assertNotNull(result)
    }


    @Test
    fun `staying non-historical implicitly with start_date patched to null returns error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Undefined,
            current_is_historical = false,
            effective_start_date = null,
            effective_end_date = null,
        )

        assertNotNull(result)
    }


    @Test
    fun `staying non-historical implicitly with start_date untouched and already set returns no error`()
    {
        val result = validateHistoricalTransaction(
            is_historical = Patch.Undefined,
            current_is_historical = false,
            effective_start_date = LocalDate.now(),
            effective_end_date = null,
        )

        assertNull(result)
    }


    @Test
    fun `non-historical record with end_date set but no start_date still returns error`()
    {
        // guards against a bug where only start_date is checked and end_date-without-start_date slips through
        val result = validateHistoricalTransaction(
            is_historical = Patch.Value(false),
            current_is_historical = true,
            effective_start_date = null,
            effective_end_date = LocalDate.now(),
        )

        assertNotNull(result)
    }
}
