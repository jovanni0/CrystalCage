import type { SeriesCreateRequest } from "@root/dtos/SeriesCreateRequest"
import type { SeriesResponse } from "@root/dtos/SeriesResponse"
import type { Logger } from "@root/loggers/Logger"
import { attempt, type AsyncResult } from "../utils/result"
import type { APIClient } from "../clients/api-client.interface"



export class SeriesRepository
{
    private loaded_at: number = 0

    public all: SeriesResponse[] = $state([])


    constructor(
        private api: APIClient, 
        private logger: Logger
    ) {}


    public async load(max_age_ms = 60_000)
    {
        if (Date.now() - this.loaded_at < max_age_ms) return

        this.all = await this.api.getSeries(null, null)
        this.loaded_at = Date.now()
    }


    public invalidate() 
    { 
        this.loaded_at = 0 
    }
    

    public reset()
    { 
        this.all = []
        this.loaded_at = 0 
    }


    public async create(request: SeriesCreateRequest): AsyncResult<SeriesResponse>
    {
        return attempt(this.logger, "create series", async () =>
        {
            const series = await this.api.createSeries(request)

            this.all.push(series)
            
            return series
        })
    }
}