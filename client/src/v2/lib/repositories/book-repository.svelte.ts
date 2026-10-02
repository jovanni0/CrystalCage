import type { BookCreateRequest } from "@root/dtos/BookCreateRequest"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import { attempt, type AsyncResult } from "../utils/result"
import type { Logger } from "@root/loggers/Logger"
import type { BookUpdateRequest } from "@root/dtos/BookUpdateRequest"
import type { APIClient } from "../clients/api-client.interface"



export class BookRepository
{
    private loaded_at: number = 0

    public all: BookSummaryResponse[] = $state([])


    constructor(private api: APIClient, private logger: Logger) 
    {}


    public async load(max_age_ms = 60_000)
    {
        if (Date.now() - this.loaded_at < max_age_ms) return

        this.all = await this.api.getBooks(null, null, null, null)
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


    public async create(request: BookCreateRequest): AsyncResult<BookSummaryResponse>
    {
        return attempt(this.logger, "create book", async () =>
        {
            const book_summary = await this.api.createBook(request)
            
            this.all.push(book_summary)
            
            return book_summary
        })
    }


    public async update(id: string, request: BookUpdateRequest): AsyncResult<BookSummaryResponse>
    {
        return attempt(this.logger, "update book", async () =>
        {
            const book_summary = await this.api.updateBook(id, request)

            this.all = this.all.filter(it => it.id !== id)
            this.all.push(book_summary)

            return book_summary
        })
    }


    public async delete(id: string): AsyncResult<boolean>
    {
        return attempt(this.logger, "delete book", async () =>
        {
            await this.api.deleteBook(id)

            this.all = this.all.filter(it => it.id !== id)

            return true
        })
    }
}