import type { EditionResponse }   from "@root/dtos/EditionResponse"
import type { Logger }            from "@root/loggers/Logger"
import type { EditionRepository } from "../repositories/edition-repository.svelte"
import type { AsyncResult }       from "../utils/result"
import      { describeError }     from "../utils/result"
import      { generateEphemeralId } from "@root/utils/IdGenerator"



/*
* A picked book file that is not on the server yet. Drafts are only kept while the manager page is open.
*/
export type EditionDraft = {
    local_id: string
    file:     File
    label:    string
    format:   string
    status:   "pending" | "uploading" | "failed"
    error:    string | null
}


export type EditionManagerDependencies = {
    editions: EditionRepository
}



export class EditionManagerStore
{
    public drafts:        EditionDraft[] = $state([])
    public is_uploading:  boolean        = $state(false)
    public error_message: string | null  = $state(null)


    private constructor(
        public readonly book_id: string,
        private dependencies: EditionManagerDependencies,
        private logger: Logger,
    )
    {}


    public get editions(): EditionResponse[]
    {
        return this.dependencies.editions.get(this.book_id).toSorted((a, b) => b.added_at.localeCompare(a.added_at))
    }


    public get has_editions(): boolean
    {
        return this.editions.length > 0
    }


    public get has_drafts(): boolean
    {
        return this.drafts.length > 0
    }


    public static async create(book_id: string, dependencies: EditionManagerDependencies, logger: Logger): AsyncResult<EditionManagerStore>
    {
        const store  = new EditionManagerStore(book_id, dependencies, logger)
        const result = await dependencies.editions.load(book_id)

        if (!result.ok)
        {
            return result
        }

        return { ok: true, value: store }
    }


    /*
    * Adds picked files to the upload queue. The format is guessed from the file extension and can be changed before uploading.
    */
    public addFiles(files: Iterable<File>): void
    {
        for (const file of files)
        {
            this.drafts.push({
                local_id: generateEphemeralId(),
                file:     file,
                label:    "",
                format:   EditionManagerStore.formatOf(file),
                status:   "pending",
                error:    null,
            })
        }
    }


    public removeDraft(local_id: string): void
    {
        const draft = this.drafts.find(it => it.local_id === local_id)

        if (!draft || draft.status === "uploading")
        {
            return
        }

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

            const result = await this.dependencies.editions.create({
                book_id:      this.book_id,
                label:        draft.label.trim() || null,
                release_date: null,
                format:       draft.format.trim().toLowerCase() || "unknown",
                file:         draft.file,
            })

            if (!result.ok)
            {
                draft.status = "failed"
                draft.error  = describeError(result.error)

                continue
            }

            this.drafts = this.drafts.filter(it => it.local_id !== draft.local_id)
        }

        this.is_uploading = false
    }


    public async renameEdition(edition: EditionResponse, label: string): Promise<void>
    {
        const new_label = label.trim() || null

        if (new_label === edition.label)
        {
            return
        }

        const result = await this.dependencies.editions.update(edition.id, { label: new_label })

        if (!result.ok)
        {
            this.error_message = `Could not rename the edition. ${describeError(result.error)}`
        }
    }


    public async deleteEdition(edition: EditionResponse): Promise<void>
    {
        const result = await this.dependencies.editions.delete(edition)

        if (!result.ok)
        {
            this.error_message = `Could not delete the edition. ${describeError(result.error)}`
        }
    }


    public dispose(): void
    {
        this.drafts = []

        this.logger.debug("edition manager closed")
    }


    private static formatOf(file: File): string
    {
        const parts = file.name.split(".")

        if (parts.length < 2)
        {
            return "unknown"
        }

        return parts.at(-1)!.toLowerCase()
    }
}
