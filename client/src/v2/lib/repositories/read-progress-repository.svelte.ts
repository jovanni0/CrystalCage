import type { Logger } from "@root/loggers/Logger";
import type { ApiReadProgress } from "../clients/api-read-progress";
import type { ReadProgressCreateRequest, ReadProgressResponse } from "../dtos/read-progress.dto";
import { ListCacheGrouped } from "./_list-cache-grouped.svelte";
import { attempt, type AsyncResult } from "../utils/result";



export class ReadProgressRepository extends ListCacheGrouped<ReadProgressResponse>
{
    constructor(private api: ApiReadProgress, logger: Logger)
    {
        super(logger, "read progress")
    }


    protected fetchGroup(key: string): Promise<ReadProgressResponse[]> 
    {
        return this.api.getAllByReadId(key)
    }


    protected groupOf(item: ReadProgressResponse): string 
    {
        return item.read_record_id
    }


    public create(request: ReadProgressCreateRequest): AsyncResult<ReadProgressResponse>
    {
        return attempt(this.logger, "create progress record", async () => 
        {
            const new_record = await this.api.create(request)

            this.add(new_record)

            return new_record
        })
    }
}