import type { AppError, 
              AsyncResult } from "../utils/result"
import type { Logger } from "@root/loggers/Logger"
import      { SvelteMap } from "svelte/reactivity"
import      { attempt } from "../utils/result"




export abstract class ListCacheGrouped<type_T extends { id: string }>
{
    private groups = new SvelteMap<string, type_T[]>()
    private errors = new SvelteMap<string, AppError>()

    private loaded_at      = new Map<string, number>()
    private in_flight      = new Map<string, AsyncResult<type_T[]>>()
    private key_generation = new Map<string, number>()
    private generation     = 0


    constructor(protected logger: Logger, private label: string) 
    {}


    /*
    * Fetch every item of one group, eg: all editions of a book.
    */
    protected abstract fetchGroup(key: string): Promise<type_T[]>

    /*
    * Which group an item belongs to, eg: `edition.book_id `.
    */
    protected abstract groupOf(item: type_T): string


    public get(key: string): type_T[]
    { 
        return this.groups.get(key) ?? [] 
    }
    
    
    public isLoaded(key: string): boolean
    { 
        return this.groups.has(key) 
    }
    
    
    public errorOf(key: string): AppError | null
    { 
        return this.errors.get(key) ?? null 
    }


    public load(key: string, max_age_ms = 60_000): AsyncResult<type_T[]>
    {
        const cached = this.groups.get(key)
        const age    = Date.now() - (this.loaded_at.get(key) ?? 0)

        if (cached && age < max_age_ms) 
        {
            return Promise.resolve({ 
                ok: true, 
                value: cached 
            })
        }

        const pending = this.in_flight.get(key)
        
        if (pending) return pending

        const generation     = this.generation
        const key_generation = this.key_generation.get(key) ?? 0
        const is_current     = () => generation === this.generation && key_generation === (this.key_generation.get(key) ?? 0)

        const request: AsyncResult<type_T[]> = attempt(this.logger, `load ${this.label} for ${key}`, async () =>
        {
            const items = await this.fetchGroup(key)

            if (is_current())
            {
                this.groups.set(key, items)
                this.loaded_at.set(key, Date.now())
                this.errors.delete(key)
            }

            return items
        })
        .then(result =>
        {
            if (!result.ok && is_current()) 
            {
                this.errors.set(key, result.error)
            }
            
            return result
        })
        .finally(() =>
        {
            if (this.in_flight.get(key) === request) 
            {
                this.in_flight.delete(key)
            }
        })

        this.in_flight.set(key, request)

        return request
    }


    public invalidate(key?: string)
    {
        if (key) this.loaded_at.delete(key)
        else     this.loaded_at.clear()
    }

    /*
    * Drop one group entirely, eg: when its book is deleted 
    */
    public forget(key: string)
    {
        this.key_generation.set(key, (this.key_generation.get(key) ?? 0) + 1)
        this.groups.delete(key)
        this.errors.delete(key)
        this.loaded_at.delete(key)
        this.in_flight.delete(key)
    }

    public reset()
    {
        this.generation++
        this.groups.clear()
        this.errors.clear()
        this.loaded_at.clear()
        this.in_flight.clear()
        this.key_generation.clear()
    }


    /*
    * Only touch groups that are loaded; an unloaded group fetches fresh data anyway.
    */
    private change(key: string, fn: (list: type_T[]) => type_T[])
    {
        const list = this.groups.get(key)

        if (list) 
        {
            this.groups.set(key, fn(list))
        }
    }

    protected add(item: type_T)
    { 
        this.change(this.groupOf(item), list => [...list, item]) 
    }
    
    
    protected replace(item: type_T)
    { 
        this.change(this.groupOf(item), list => list.map(it => it.id === item.id ? item : it)) 
    }
    
    
    protected remove(key: string, id: string)
    { 
        this.change(key, list => list.filter(it => it.id !== id)) 
    }
}