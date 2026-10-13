import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse";



export type ReadStatusSummary = 
    | { kind: "unread" }
    | { kind: "finished", count: number }
    | { kind: "dnf" }



export class ReadRecordRules
{
    public static isOpen(record: ReadRecordResponse): boolean
    {
        return record.end_date === null && !record.is_historical
    }


    public static isFinished(record: ReadRecordResponse): boolean
    {
        return !record.did_not_finish && (record.end_date !== null || record.is_historical)
    }


    public static currentReadOf(records: ReadRecordResponse[]): ReadRecordResponse | null
    {
        return records.find(it => ReadRecordRules.isOpen(it)) ?? null
    }


    public static summaryOf(records: ReadRecordResponse[]): ReadStatusSummary
    {
        const finished_count = records.filter(it => ReadRecordRules.isFinished(it)).length

        if (finished_count > 0)
        {
            return {
                kind: "finished",
                count: finished_count
            }
        }

        if (records.some(it => it.did_not_finish))
        {
            return { kind: "dnf" }
        }

        return { kind: "unread" }
    }
}