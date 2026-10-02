import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { APIClient } from "../clients/api-client.interface"
import type { Logger } from "@root/loggers/Logger"
import type { EditionCreateRequest } from "@root/dtos/EditionCreateRequest"
import type { EditionUpdateRequest } from "@root/dtos/EditionUpdateRequest"
import      { SvelteMap } from "svelte/reactivity"
import      { attempt, type AsyncResult } from "../utils/result"



export class EditionRepository
{
    /*
    * Tracks the latest request cycle. Used for discarding in flight data after a reset. 
    */
    private generation = 0

    private by_book   = new SvelteMap<string, EditionResponse[]>()
    private loaded_at = new Map<string, number>()
    private in_flight = new Map<string, AsyncResult<EditionResponse[]>>()


    
    constructor(private api: APIClient, private logger: Logger) 
    {}


    public forBook(book_id: string): EditionResponse[]
    {
        return this.by_book.get(book_id) ?? []
    }


    public load(book_id: string, max_age_ms = 60_000): AsyncResult<EditionResponse[]>
    {
        const cached = this.by_book.get(book_id)
        const age    = Date.now() - (this.loaded_at.get(book_id) ?? 0)

        if (cached && age < max_age_ms) 
        {
            return Promise.resolve({ 
                ok: true, 
                value: cached 
            })
        }

        const pending = this.in_flight.get(book_id)
        
        if (pending) return pending

        const generation = this.generation
        const request: AsyncResult<EditionResponse[]> = attempt(this.logger, "load editions", async () =>
        {
            const editions = await this.api.getEditions(book_id)

            // ignore results from before a reset/forget
            if (generation === this.generation)
            {
                this.by_book.set(book_id, editions)
                this.loaded_at.set(book_id, Date.now())
            }

            return editions
        })
        .finally(() =>
        {
            if (this.in_flight.get(book_id) === request) 
            {
                this.in_flight.delete(book_id)
            }
        })

        this.in_flight.set(book_id, request)
        return request
    }


    public create(request: EditionCreateRequest): AsyncResult<EditionResponse>
    {
        return attempt(this.logger, "create edition", async () =>
        {
            const edition = await this.api.createEdition(request)

            this.replaceList(request.book_id, list => [...list, edition])
            
            return edition
        })
    }


    public update(book_id: string, id: string, request: EditionUpdateRequest): AsyncResult<EditionResponse>
    {
        return attempt(this.logger, "update edition", async () =>
        {
            const edition = await this.api.updateEdition(id, request)

            this.replaceList(book_id, list => list.map(it => it.id === id ? edition : it))

            return edition
        })
    }


    public delete(book_id: string, id: string): AsyncResult<void>
    {
        return attempt(this.logger, "delete edition", async () =>
        {
            await this.api.deleteEdition(id)

            this.replaceList(book_id, list => list.filter(it => it.id !== id))
        })
    }


    /*
    * Used to discard editions when a book is deleted. Similar with DB's `ON DELETE CASCADE`. 
    */
    public forget(book_id: string)
    {
        this.generation++
        this.by_book.delete(book_id)
        this.loaded_at.delete(book_id)
        this.in_flight.delete(book_id)
    }

    public reset()
    {
        this.generation++
        this.by_book.clear()
        this.loaded_at.clear()
        this.in_flight.clear()
    }


    /* 
    * only touch lists that are already loaded; an unloaded book will fetch fresh data anyway 
    */
    private replaceList(book_id: string, change: (list: EditionResponse[]) => EditionResponse[])
    {
        const list = this.by_book.get(book_id)

        if (list) 
        {
            this.by_book.set(book_id, change(list))
        }
    }
}