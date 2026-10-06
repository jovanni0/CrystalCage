import type { APIClient }           from "../clients/api-client.interface"
import type { AsyncResult }         from "../utils/result"
import type { Logger }              from "@root/loggers/Logger"
import type { SeriesCreateRequest } from "@root/dtos/SeriesCreateRequest"
import type { SeriesResponse }      from "@root/dtos/SeriesResponse"
import      { attempt }             from "../utils/result"
import      { ListCache }           from "./_list-cache.svelte"



export class SeriesRepository extends ListCache<SeriesResponse>
{
    constructor(private api: APIClient, logger: Logger) 
    {
        super(logger, "series")
    }


    protected fetchAll(): Promise<SeriesResponse[]> 
    {
        return this.api.getSeries(null, null)
    }


    public async create(request: SeriesCreateRequest): AsyncResult<SeriesResponse>
    {
        return attempt(this.logger, "create series", async () =>
        {
            const series = await this.api.createSeries(request)

            this.add(series)
            
            return series
        })
    }
}