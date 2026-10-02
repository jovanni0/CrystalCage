import type { AuthorCreateRequest } from "@root/dtos/AuthorCreateRequest"
import type { AuthorResponse }      from "@root/dtos/AuthorResponse"
import type { Logger }              from "@root/loggers/Logger"
import type { APIClient }           from "../clients/api-client.interface"
import type { AsyncResult }         from "../utils/result"
import      { attempt, }            from "../utils/result"
import      { ListCache }           from "./_list-cache.svelte"



export class AuthorRepository extends ListCache<AuthorResponse>
{
    constructor(private api: APIClient, logger: Logger) 
    {
        super(logger, "authors")
    }


    protected fetchAll(): Promise<AuthorResponse[]> 
    {
        return this.api.getAuthors()
    }


    public async create(request: AuthorCreateRequest): AsyncResult<AuthorResponse>
    {
        return attempt(this.logger, "create author", async () =>
        {
            const author = await this.api.createAuthor(request)
            
            this.add(author)
            
            return author
        })
    }
}