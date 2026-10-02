import type { UniverseCreateRequest } from "@root/dtos/UniverseCreateRequest"
import type { UniverseResponse } from "@root/dtos/UniverseResponse"
import type { Logger } from "@root/loggers/Logger"
import { attempt, type AsyncResult } from "../utils/result"
import type { APIClient } from "../clients/api-client.interface"



export class UniverseRepository
{
    private loaded_at: number = 0

    public all: UniverseResponse[] = $state([])


    constructor(
        private api: APIClient, 
        private logger: Logger
    ) {}


    public async load(max_age_ms = 60_000)
    {
        if (Date.now() - this.loaded_at < max_age_ms) return

        this.all = await this.api.getUniverses(null)
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


    public async create(request: UniverseCreateRequest): AsyncResult<UniverseResponse>
    {
        return attempt(this.logger, "create universe", async () =>
        {
            const universe = await this.api.createUniverse(request)

            this.all.push(universe)
            
            return universe
        })
    }
}