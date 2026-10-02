import type { APIClient }           from "../clients/api-client.interface"
import type { AsyncResult }         from "../utils/result"
import type { BookCreateRequest }   from "@root/dtos/BookCreateRequest"
import type { BookDetailResponse }  from "@root/dtos/BookDetailResponse"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import type { BookUpdateRequest }   from "@root/dtos/BookUpdateRequest"
import type { Logger }              from "@root/loggers/Logger"
import      { attempt }             from "../utils/result"
import      { ListCache }           from "./_list-cache.svelte"



export class BookRepository extends ListCache<BookSummaryResponse>
{
    constructor(private api: APIClient, logger: Logger) 
    {
        super(logger, "books")
    }


    protected fetchAll(): Promise<BookSummaryResponse[]> 
    {
        return this.api.getBooks(null, null, null, null)
    }


    public async create(request: BookCreateRequest): AsyncResult<BookSummaryResponse>
    {
        return attempt(this.logger, "create book", async () =>
        {
            const book_summary = await this.api.createBook(request)
            
            this.add(book_summary)
            
            return book_summary
        })
    }


    public async update(id: string, request: BookUpdateRequest): AsyncResult<BookSummaryResponse>
    {
        return attempt(this.logger, "update book", async () =>
        {
            const book_summary = await this.api.updateBook(id, request)

            this.replace(book_summary)

            return book_summary
        })
    }


    public async delete(id: string): AsyncResult<boolean>
    {
        return attempt(this.logger, "delete book", async () =>
        {
            await this.api.deleteBook(id)

            this.remove(id)

            return true
        })
    }


    public async details(id: string): AsyncResult<BookDetailResponse>
    {
        return attempt(this.logger, "book details", async () =>
        {
            const book_details = await this.api.getBook(id)

            return book_details
        })
    }
}