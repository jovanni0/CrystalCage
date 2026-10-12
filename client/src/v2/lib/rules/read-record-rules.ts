import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse";



export class ReadRecordRules
{
    public static isOpen(record: ReadRecordResponse): boolean
    {
        return record.end_date === null && !record.is_historical
    }


    public static currentReadOf(records: ReadRecordResponse[]): ReadRecordResponse | null
    {
        return records.find(it => ReadRecordRules.isOpen(it)) ?? null
    }
}