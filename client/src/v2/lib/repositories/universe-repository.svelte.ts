import type { APIClient }             from "../clients/api-client.interface"
import type { AsyncResult }           from "../utils/result"
import type { UniverseCreateRequest } from "@root/dtos/UniverseCreateRequest"
import type { UniverseResponse }      from "@root/dtos/UniverseResponse"
import type { Logger }                from "@root/loggers/Logger"
import      { attempt }               from "../utils/result"
import      { ListCache }             from "./_list-cache.svelte"



export class UniverseRepository extends ListCache<UniverseResponse>
{
    constructor(private api: APIClient, logger: Logger) 
    {
        super(logger, "universes")
    }


    protected fetchAll(): Promise<UniverseResponse[]> 
    {
        return this.api.getUniverses(null)
    }


    public async create(request: UniverseCreateRequest): AsyncResult<UniverseResponse>
    {
        return attempt(this.logger, "create universe", async () =>
        {
            const universe = await this.api.createUniverse(request)

            this.add(universe)
            
            return universe
        })
    }
}