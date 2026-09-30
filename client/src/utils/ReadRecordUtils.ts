import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse";



export function getIntervalInDays(start: string | null, end: string | null): number | null
{
    if (start === null || end === null)
    {
        return null
    }

    const start_date = new Date(start).getTime()
    const end_date = new Date(end).getTime()
    const time_interval = end_date - start_date
    const days = Math.round( time_interval / 86_400_000) + 1

    return days
}