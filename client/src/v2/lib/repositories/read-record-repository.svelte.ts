import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse";
import { ListCacheGrouped } from "./_list-cache-grouped.svelte";
import type { ApiReadRecords } from "../clients/api-read-records";
import type { Logger } from "@root/loggers/Logger";
import type { ReadRecordCreateRequest } from "@root/dtos/ReadRecordCreateRequest";
import { attempt, type AsyncResult } from "../utils/result";
import type { ReadRecordUpdateRequest } from "@root/dtos/ReadRecordUpdateRequest";



export class ReadRecordRepository extends ListCacheGrouped<ReadRecordResponse>
{
    constructor(private api: ApiReadRecords, logger: Logger)
    {
        super(logger, "read progress")
    }


    protected fetchGroup(key: string): Promise<ReadRecordResponse[]> 
    {
        return this.api.getAllByBookId(key)
    }


    protected groupOf(item: ReadRecordResponse): string 
    {
        return item.book_id
    }


    public create(request: ReadRecordCreateRequest): AsyncResult<ReadRecordResponse>
    {
        return attempt(this.logger, "create read record", async () => 
        {
            const new_record = await this.api.create(request)

            this.add(new_record)

            return new_record
        })
    }


    public update(id: string, request: ReadRecordUpdateRequest): AsyncResult<ReadRecordResponse>
    {
        return attempt(this.logger, "update read record", async () => 
        {
            const new_record = await this.api.update(id, request)

            this.replace(new_record)

            return new_record
        })
    }


    public delete(read_record: ReadRecordResponse): AsyncResult<void>
    {
        return attempt(this.logger, "delete read record", async () => 
        {
            const new_record = await this.api.delete(read_record.id)

            this.remove(read_record.book_id, read_record.id)
        })
    }
}