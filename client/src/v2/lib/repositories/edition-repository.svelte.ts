import type { APIClient }            from "../clients/api-client.interface"
import type { AsyncResult }          from "../utils/result"
import type { EditionResponse }      from "@root/dtos/EditionResponse"
import type { Logger }               from "@root/loggers/Logger"
import type { EditionCreateRequest } from "@root/dtos/EditionCreateRequest"
import type { EditionUpdateRequest } from "@root/dtos/EditionUpdateRequest"
import      { attempt }              from "../utils/result"
import      { ListCacheGrouped }     from "./_list-cache-grouped.svelte"



export class EditionRepository extends ListCacheGrouped<EditionResponse>
{
    constructor(private api: APIClient, logger: Logger) 
    {
        super(logger, "editions")
    }


    protected fetchGroup(key: string): Promise<EditionResponse[]> 
    {
        return this.api.getEditions(key)
    }


    protected groupOf(item: EditionResponse): string 
    {
        return item.book_id
    }


    public create(request: EditionCreateRequest): AsyncResult<EditionResponse>
    {
        return attempt(this.logger, "create edition", async () =>
        {
            const edition = await this.api.createEdition(request)

            this.add(edition)
            
            return edition
        })
    }


    public update(id: string, request: EditionUpdateRequest): AsyncResult<EditionResponse>
    {
        return attempt(this.logger, "update edition", async () =>
        {
            const edition = await this.api.updateEdition(id, request)

            this.replace(edition)

            return edition
        })
    }


    public delete(edition: EditionResponse): AsyncResult<void>
    {
        return attempt(this.logger, "delete edition", async () =>
        {
            await this.api.deleteEdition(edition.id)

            this.remove(edition.book_id, edition.id)
        })
    }
}