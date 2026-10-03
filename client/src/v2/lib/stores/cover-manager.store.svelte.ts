import type { CoverResponse }   from "@root/dtos/CoverResponse"
import type { Logger }          from "@root/loggers/Logger"
import type { BookRepository }  from "../repositories/book-repository.svelte"
import type { CoverRepository } from "../repositories/cover-repository.svelte"
import type { AsyncResult }     from "../utils/result"
import      { describeError }   from "../utils/result"
import      { generateEphemeralId } from "@root/utils/IdGenerator"



/*
* A picked image that is not on the server yet. Drafts are only kept while the manager page is open.
*/
export type CoverDraft = {
    local_id:    string
    file:        File
    preview_url: string
    label:       string
    status:      "pending" | "uploading" | "failed"
    error:       string | null
}


export type CoverManagerDependencies = {
    covers: CoverRepository
    books:  BookRepository
}



export class CoverManagerStore
{
    public drafts:        CoverDraft[]  = $state([])
    public is_uploading:  boolean       = $state(false)
    public is_fetching:   boolean       = $state(false)
    public error_message: string | null = $state(null)


    private constructor(
        public readonly book_id: string,
        private dependencies: CoverManagerDependencies,
        private logger: Logger,
    )
    {}


    public get covers(): CoverResponse[]
    {
        return this.dependencies.covers.get(this.book_id)
    }


    public get has_covers(): boolean
    {
        return this.covers.length > 0
    }


    public get has_drafts(): boolean
    {
        return this.drafts.length > 0
    }


    public static async create(book_id: string, dependencies: CoverManagerDependencies, logger: Logger): AsyncResult<CoverManagerStore>
    {
        const store  = new CoverManagerStore(book_id, dependencies, logger)
        const result = await dependencies.covers.load(book_id)

        if (!result.ok)
        {
            return result
        }

        return { ok: true, value: store }
    }


    /*
    * Adds picked files to the upload queue.
    */
    public addFiles(files: Iterable<File>): void
    {
        for (const file of files)
        {
            this.drafts.push({
                local_id:    generateEphemeralId(),
                file:        file,
                preview_url: URL.createObjectURL(file),
                label:       "",
                status:      "pending",
                error:       null,
            })
        }
    }


    /*
    * Downloads an image through the server proxy and queues it like a picked file.
    */
    public async addFromUrl(url: string): Promise<boolean>
    {
        const trimmed_url = url.trim()

        if (trimmed_url === "")
        {
            return false
        }

        this.is_fetching   = true
        this.error_message = null

        const result = await this.dependencies.covers.fetchFromUrl(trimmed_url)

        this.is_fetching = false

        if (!result.ok)
        {
            this.error_message = `Could not get the image. ${describeError(result.error)}`

            return false
        }

        this.addFiles([result.value])

        return true
    }


    public removeDraft(local_id: string): void
    {
        const draft = this.drafts.find(it => it.local_id === local_id)

        if (!draft || draft.status === "uploading")
        {
            return
        }

        URL.revokeObjectURL(draft.preview_url)
        this.drafts = this.drafts.filter(it => it.local_id !== local_id)
    }


    /*
    * Uploads every pending or failed draft, one at a time. Uploaded drafts leave the queue; failed ones stay with their error.
    */
    public async uploadDrafts(): Promise<void>
    {
        if (this.is_uploading)
        {
            return
        }

        this.is_uploading  = true
        this.error_message = null

        for (const draft of this.drafts.filter(it => it.status !== "uploading"))
        {
            draft.status = "uploading"
            draft.error  = null

            const result = await this.dependencies.covers.create({
                book_id: this.book_id,
                label:   draft.label.trim() || null,
                file:    draft.file,
            })

            if (!result.ok)
            {
                draft.status = "failed"
                draft.error  = describeError(result.error)

                continue
            }

            URL.revokeObjectURL(draft.preview_url)
            this.drafts = this.drafts.filter(it => it.local_id !== draft.local_id)

            if (!this.covers.some(it => it.is_default))
            {
                await this.setDefault(result.value.id)
            }
        }

        this.is_uploading = false
    }


    public async setDefault(cover_id: string): Promise<void>
    {
        const result = await this.dependencies.books.update(this.book_id, { cover_id })

        if (!result.ok)
        {
            this.error_message = `Could not change the default cover. ${describeError(result.error)}`

            return
        }

        this.dependencies.covers.markDefault(this.book_id, cover_id)
    }


    public async renameCover(cover: CoverResponse, label: string): Promise<void>
    {
        const new_label = label.trim() || null

        if (new_label === cover.label)
        {
            return
        }

        const result = await this.dependencies.covers.update(cover.id, { label: new_label })

        if (!result.ok)
        {
            this.error_message = `Could not rename the cover. ${describeError(result.error)}`
        }
    }


    public async deleteCover(cover: CoverResponse): Promise<void>
    {
        const result = await this.dependencies.covers.delete(cover)

        if (!result.ok)
        {
            this.error_message = `Could not delete the cover. ${describeError(result.error)}`

            return
        }

        // the server decides what happens to the default cover, so refresh both sides
        this.dependencies.books.invalidate()
        await this.dependencies.covers.load(this.book_id, 0)
    }


    /*
    * Frees the preview images. Call when the page is closed.
    */
    public dispose(): void
    {
        for (const draft of this.drafts)
        {
            URL.revokeObjectURL(draft.preview_url)
        }

        this.drafts = []

        this.logger.debug("cover manager closed")
    }
}
