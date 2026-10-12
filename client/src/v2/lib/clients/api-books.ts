import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";
import type { SessionHttp } from "./session-http";



export class ApiBooks
{
    constructor(private session: SessionHttp)
    {}


    public getBooks(): Promise<BookSummaryResponse>
    {
        return this.session.send<BookSummaryResponse>("/books")
    }
}