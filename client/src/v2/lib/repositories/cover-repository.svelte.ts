import type { APIClient }          from "../clients/api-client.interface"
import type { AsyncResult }        from "../utils/result"
import type { CoverCreateRequest } from "@root/dtos/CoverCreateRequest"
import type { CoverResponse }      from "@root/dtos/CoverResponse"
import type { CoverUpdateRequest } from "@root/dtos/CoverUpdateRequest"
import type { Logger }             from "@root/loggers/Logger"
import      { attempt }            from "../utils/result"
import      { ListCacheGrouped }   from "./_list-cache-grouped.svelte"



/*
* Covers of each book, grouped by `book_id`.
*/
export class CoverRepository extends ListCacheGrouped<CoverResponse>
{
    constructor(private api: APIClient, logger: Logger)
    {
        super(logger, "covers")
    }


    protected fetchGroup(book_id: string): Promise<CoverResponse[]>
    {
        return this.api.getCovers(book_id)
    }


    protected groupOf(cover: CoverResponse): string
    {
        return cover.book_id
    }


    public create(request: CoverCreateRequest): AsyncResult<CoverResponse>
    {
        return attempt(this.logger, "create cover", async () =>
        {
            const cover = await this.api.createCover(request)

            this.add(cover)

            return cover
        })
    }


    public update(id: string, request: CoverUpdateRequest): AsyncResult<CoverResponse>
    {
        return attempt(this.logger, "update cover", async () =>
        {
            const cover = await this.api.updateCover(id, request)

            this.replace(cover)

            return cover
        })
    }


    public delete(cover: CoverResponse): AsyncResult<void>
    {
        return attempt(this.logger, "delete cover", async () =>
        {
            await this.api.deleteCover(cover.id)

            this.remove(cover.book_id, cover.id)
        })
    }


    /*
    * The default cover is stored on the book (`cover_id`), so after the book is updated this only syncs the local flags.
    */
    public markDefault(book_id: string, cover_id: string): void
    {
        this.change(book_id, list => list.map(it => ({ ...it, is_default: it.id === cover_id })))
    }


    /*
    * Downloads an image through the server's proxy, so it can be added like an uploaded file.
    */
    public fetchFromUrl(url: string): AsyncResult<File>
    {
        return attempt(this.logger, "fetch cover from url", async () =>
        {
            const blob      = await this.api.fetchImageFromUrl(url)
            const extension = blob.type.split("/").at(1) ?? "jpg"

            return new File([blob], `cover.${extension}`, { type: blob.type })
        })
    }
}
