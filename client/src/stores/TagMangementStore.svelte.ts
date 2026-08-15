import type { SeriesResponse }    from "@root/dtos/SeriesResponse"
import type { TagCreateRequest }  from "@root/dtos/TagCreateRequest"
import type { TagDetailResponse } from "@root/dtos/TagDetailResponse"
import type { TagUpdateRequest }  from "@root/dtos/TagUpdateRequest"
import { ConflictError } from "@root/errors/ConflictError"
import type { Logger }            from "@root/loggers/Logger"
import type { StoreStatus }       from "@root/types/StoreStatus"
import type { ApiClient }         from "@root/utils/ApiClient"
import      { buildTagUsage }     from "@root/utils/LibraryGroupings"



// NOTE: wouldn't it be easier to just return the series name instead of the ID? it means another JOIN server-side, but it could be faster/cheaper then client-side 
// (unless caching makes a big difference).
export type TagBookLink = {
    book_id:     string
    book_title:  string
    series_name: string | null
    cover_id:    string | null
}



export class TagManagementStore
{
    private series:           SeriesResponse[]         = $state([])
    private tag:              TagDetailResponse | null = $state(null)
    private added_book_ids:   string[]                 = $state([])
    private removed_book_ids: string[]                 = $state([])

    public status:        StoreStatus   = $state("IDLE")
    public name:          string | null = $state(null)
    public description:   string | null = $state(null)
    public error_message: string | null = $state(null)

    public book_linked: TagBookLink[] = $derived(this.tag?.books ? buildTagUsage(this.tag.books.filter(it => !this.removed_book_ids.includes(it.id)), this.series) : [])
    public link_count:  number        = $derived(this.book_linked.length)


    constructor(
        private api: ApiClient, 
        private logger: Logger, 
        private id: string | null = null
    )
    { }


    public async initialize()
    {
        if (this.id !== null)
        {
            await this.fetchAll(this.id)
        }
        else
        {
            this.status = "LOADED"
        }
    }

    
    private async fetchAll(id: string)
    {
        this.status = "FETCHING"

        try
        {
            const [series, tag] = await Promise.all([
                this.api.getSeries(null, null),
                this.api.getTag(id)
            ])

            this.series = series
            this.tag = tag
            this.name = tag.name
            this.description = tag.description
            this.status = "LOADED"
            this.error_message = null
        }
        catch (e)
        {
            this.logger.error("failed to fetch tag info", { error: e })

            this.error_message = "Failed to fetch tag info from server."
            this.status = "FETCH_FAILED"
        }
    }


    public async save()
    {
        if (this.id === null)
        {
            return await this.createTag()
        }
        else
        {
            return await this.updateTag()
        }
    }


    private async createTag(): Promise<boolean>
    {
        if (this.name === null || this.name === "")
        {
            this.logger.warn("tag name cannot be null or empty", { name: this.name })
            this.error_message = "Tag name cannot be empty"

            return false
        }

        const tag: TagCreateRequest = {
            name: this.name,
            description: this.description,
        }

        try
        {
            await this.api.createTag(tag)

            this.error_message = null

            return true
        }
        catch (e)
        {
            this.logger.error("failed to create tag", { error: e })

            switch (true)
            {
                case e instanceof ConflictError: this.error_message = "A tag with the same name already exists."; break;
                default:                         this.error_message = "Server error. See logs."
            }

            return false
        }
    }


    private async updateTag(): Promise<boolean>
    {
        if (this.tag === null || this.id === null)
        {
            this.logger.warn("cannot update the tag if no tag was fetched")
            this.error_message = "Application error. See logs."

            return false
        }

        if (this.name === null || this.name === "")
        {
            this.logger.warn("tag name cannot be null or empty", { name: this.name })
            this.error_message = "Tag name cannot be empty"

            return false
        }

        const tag: TagUpdateRequest = {
            name: this.name !== this.tag.name ? this.name : undefined,
            description: this.description !== this.tag.description ? this.description : undefined,
            added_book_ids: this.added_book_ids.length > 0 ? this.added_book_ids : undefined,
            removed_book_ids: this.removed_book_ids.length > 0 ? this.removed_book_ids : undefined,
        }

        try
        {
            await this.api.updateTag(this.id, tag)

            this.error_message = null

            return true
        }
        catch (e)
        {
            this.logger.error("failed to update tag", { error: e })
            this.error_message = "Server error. See logs."
            
            return false
        }
    }


    public removeLink(book_id: string)
    {
        if (!this.removed_book_ids.includes(book_id))
        {
            this.logger.info("book ID put into 'removed' list", { book_id: book_id })
            this.removed_book_ids.push(book_id)
        }
    }


    public async delete(): Promise<boolean>
    {
        if (this.id)
        {
            const result = await this.api.deleteTag(this.id)

            if (result === false)
            {
                this.error_message = "Could not delete tag."
            }

            return result
        }

        return false
    }
}