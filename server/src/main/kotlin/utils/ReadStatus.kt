package dev.jovanni0.utils

import dev.jovanni0.data_types.ReadRecordAggregate
import dev.jovanni0.data_types.ReadStatus



fun computeReadStatus(records: List<ReadRecordAggregate>): Pair<ReadStatus, Int>
{
    if (records.isEmpty())
        return ReadStatus.UNREAD to 0

    val historical = records.firstOrNull { it.is_historical }
    val dated = records.filter { !it.is_historical }

    if (historical != null && dated.isEmpty())
    {
        if (historical.dnf)
        {
            return ReadStatus.DNF to 0
        }

        return ReadStatus.READ to 1
    }

    val open_reads = dated.filter { it.end_date == null }
    val completed_reads = dated.filter { it.end_date != null }
    val finished_count = completed_reads.count { !it.dnf }
    val has_dnf = completed_reads.any { it.dnf } || historical?.dnf == true
    val historical_bonus = if (historical != null && !historical.dnf) 1 else 0
    val read_count = finished_count + historical_bonus

    if (open_reads.isNotEmpty())
        return ReadStatus.IN_PROGRESS to read_count

    if (has_dnf)
    {
        if (read_count > 0)
        {
            return ReadStatus.READ to read_count
        }

        return ReadStatus.DNF to 0
    }

    return ReadStatus.READ to read_count
}
