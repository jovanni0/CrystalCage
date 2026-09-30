package utils

import dev.jovanni0.data_types.ReadRecordAggregate
import dev.jovanni0.data_types.ReadStatus
import dev.jovanni0.utils.computeReadStatus
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals



class ReadStatusTests
{
    @Test
    fun `empty list returns UNREAD with 0`()
    {
        val records = listOf<ReadRecordAggregate>()

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.UNREAD, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `single record no end_date dnf false returns IN_PROGRESS with 0`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, false)
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.IN_PROGRESS, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `one open record + one completed non-dnf record returns IN_PROGRESS with count 1`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, false),
            ReadRecordAggregate(LocalDate.now(), false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.IN_PROGRESS, read_status)
        assertEquals(1, read_count)
    }


    @Test
    fun `one open record + two completed non-dnf records returns IN_PROGRESS, count 2`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, false),
            ReadRecordAggregate(LocalDate.now(), false, false),
            ReadRecordAggregate(LocalDate.now(), false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.IN_PROGRESS, read_status)
        assertEquals(2, read_count)
    }


    @Test
    fun `one open record + completed dnf record only (no completed non-dnf) returns IN_PROGRESS, count 0`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, false),
            ReadRecordAggregate(LocalDate.now(), true, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.IN_PROGRESS, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `one record dnf + no other records returns DNF, count 0`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(LocalDate.now(), true, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.DNF, read_status)
        assertEquals(0, read_count)
    }

    @Test
    fun `all records completed, one dnf=true, one completed non-dnf returns READ, count 1`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(LocalDate.now(), true, false),
            ReadRecordAggregate(LocalDate.now(), false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(1, read_count)
    }

    @Test
    fun `all records completed, one dnf=true, two completed non-dnf returns READ, count 2`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(LocalDate.now(), true, false),
            ReadRecordAggregate(LocalDate.now(), false, false),
            ReadRecordAggregate(LocalDate.now(), false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(2, read_count)
    }

    @Test
    fun `all records completed, all dnf=true returns DNF, count 0`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(LocalDate.now(), true, false),
            ReadRecordAggregate(LocalDate.now(), true, false),
            ReadRecordAggregate(LocalDate.now(), true, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.DNF, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `only a historical record, no dated records returns READ, count 1`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, true),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(1, read_count)
    }

    @Test
    fun `historical record + one dated records returns READ, count 2`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, true),
            ReadRecordAggregate(LocalDate.now(), false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(2, read_count)
    }


    @Test
    fun `two historical records returns READ, count 1`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, false, true),
            ReadRecordAggregate(null, false, true),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(1, read_count)
    }


    @Test
    fun `historical record only, dnf=true returns DNF, count 0`()
    {
        val records = listOf<ReadRecordAggregate>(
            ReadRecordAggregate(null, true, true),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.DNF, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `historical non-dnf + one open record returns IN_PROGRESS with historical counted`()
    {
        val records = listOf(
            ReadRecordAggregate(null, false, true),
            ReadRecordAggregate(null, false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.IN_PROGRESS, read_status)
        assertEquals(1, read_count)
    }


    @Test
    fun `historical dnf + one open record returns IN_PROGRESS with historical not counted`()
    {
        val records = listOf(
            ReadRecordAggregate(null, true, true),
            ReadRecordAggregate(null, false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.IN_PROGRESS, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `historical non-dnf + dated dnf-only records returns READ, historical rescues status`()
    {
        val records = listOf(
            ReadRecordAggregate(null, false, true),
            ReadRecordAggregate(LocalDate.now(), true, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(1, read_count)
    }


    @Test
    fun `historical dnf + dated dnf-only records returns DNF, count 0`()
    {
        val records = listOf(
            ReadRecordAggregate(null, true, true),
            ReadRecordAggregate(LocalDate.now(), true, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.DNF, read_status)
        assertEquals(0, read_count)
    }


    @Test
    fun `historical dnf + dated completed non-dnf returns READ, historical excluded from count`()
    {
        val records = listOf(
            ReadRecordAggregate(null, true, true),
            ReadRecordAggregate(LocalDate.now(), false, false),
        )

        val (read_status, read_count) = computeReadStatus(records)

        assertEquals(ReadStatus.READ, read_status)
        assertEquals(1, read_count)
    }
}
