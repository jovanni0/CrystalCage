import type { AuthorCreateRequest } from "@root/dtos/AuthorCreateRequest"
import type { AuthorResponse } from "@root/dtos/AuthorResponse"
import type { Logger } from "@root/loggers/Logger"
import      { attempt, type AsyncResult } from "../utils/result"
import type { APIClient } from "../clients/api-client.interface"



export class AuthorRepository
{
    private loaded_at: number = 0

    public all: AuthorResponse[] = $state([])


    constructor(
        private api: APIClient, 
        private logger: Logger
    ) {}


    public async load(max_age_ms = 60_000)
    {
        if (Date.now() - this.loaded_at < max_age_ms) return

        this.all = await this.api.getAuthors()
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


    public async create(request: AuthorCreateRequest): AsyncResult<AuthorResponse>
    {
        return attempt(this.logger, "create author", async () =>
        {
            const author = await this.api.createAuthor(request)
            
            this.all.push(author)
            
            return author
        })
    }
}