import type { Logger }      from "@root/loggers/Logger"
import type { AppError, 
              AsyncResult } from "../utils/result"
import      { attempt }     from "../utils/result"



export type LoadStatus = "idle" | "loading" | "ready" | "error"



export abstract class ListCache<type_T extends { id: string }>
{
    public all:    type_T[]        = $state([])
    public status: LoadStatus      = $state("idle")
    public error:  AppError | null = $state(null)

    private loaded_at  = 0
    private generation = 0
    private in_flight: AsyncResult<type_T[]> | null = null


    constructor(protected logger: Logger, private label: string) 
    {}


    /*
    * The only thing each repository must provide.
    */
    protected abstract fetchAll(): Promise<type_T[]>


    public load(max_age_ms = 60_000): AsyncResult<type_T[]>
    {
        const is_fresh = this.status === "ready" && Date.now() - this.loaded_at < max_age_ms

        if (is_fresh)
        {
            return Promise.resolve({ 
                ok: true, 
                value: this.all 
            })
        }

        if (this.in_flight) 
        {
            return this.in_flight
        }

        const generation = this.generation

        if (this.status !== "ready") 
        {
            this.status = "loading"
        }

        const request: AsyncResult<type_T[]> = attempt(this.logger, `load ${this.label}`, async () =>
        {
            const items = await this.fetchAll()

            if (generation === this.generation)
            {
                this.all       = items
                this.loaded_at = Date.now()
                this.status    = "ready"
                this.error     = null
            }

            return items
        })
        .then(result =>
        {
            if (!result.ok && generation === this.generation)
            {
                // If the old date is still usable, remain ready but expose the error.
                this.status = this.loaded_at ? "ready" : "error"
                this.error  = result.error
            }
            return result
        })
        .finally( () =>
        {
            if (this.in_flight === request) 
            {
                this.in_flight = null
            }
        })

        this.in_flight = request

        return request
    }


    /*
    * Wait only if there's nothing to show yet, otherwise refresh in the background .
    */
    public ensure(): AsyncResult<type_T[]>
    {
        if (this.status === "ready")
        {
            void this.load()

            return Promise.resolve({ 
                ok: true, 
                value: this.all 
            })
        }

        return this.load()
    }


    public invalidate() 
    { 
        this.loaded_at = 0 
    }


    public reset()
    {
        this.generation++
        this.all       = []
        this.status    = "idle"
        this.error     = null
        this.loaded_at = 0
        this.in_flight = null
    }


    public byId(id: string | null): type_T | undefined
    {
        return id ? this.all.find(it => it.id === id) : undefined
    }


    protected add(item: type_T)
    { 
        this.all = [...this.all, item] 
    }


    protected replace(item: type_T)
    { 
        this.all = this.all.map(it => it.id === item.id ? item : it) 
    }


    protected remove(id: string)
    { 
        this.all = this.all.filter(it => it.id !== id) 
    }
}