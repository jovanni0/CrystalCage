import type { BookDetailResponse } from "@root/dtos/BookDetailResponse"
import type { AuthorRepository } from "../repositories/author-repository.svelte"
import type { BookRepository } from "../repositories/book-repository.svelte"
import type { ReadRecordRepository } from "../repositories/read-record-repository.svelte"
import type { SeriesRepository } from "../repositories/series-repository.svelte"
import type { UniverseRepository } from "../repositories/universe-repository.svelte"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import { describeError, type AppError, type AsyncResult } from "../utils/result"
import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse"
import type { ReadStatusSummary } from "../rules/read-record-rules"
import      { ReadRecordRules } from "../rules/read-record-rules"
import      { AuthorRules } from "../rules/author-rules"
import { renderMarkdown } from "../utils/markdown"
import type { TagRepository } from "../repositories/tag-repository.svelte"
import type { CoverRepository } from "../repositories/cover-repository.svelte"
import type { CoverResponse } from "@root/dtos/CoverResponse"
import type { EditionRepository } from "../repositories/edition-repository.svelte"
import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { PickableEdition } from "../types/pickable-edition.type"
import type { ReadRecordCreateRequest } from "@root/dtos/ReadRecordCreateRequest"
import { edition_format_color_registry } from "@root/CompositionRoot"
import { EditionFormatColorRegistry, type EditionFileFormatDefinition } from "../utils/format-color-registry.svelte"
import type { ReadRecordUpdateRequest } from "@root/dtos/ReadRecordUpdateRequest"
import { generateEphemeralId } from "@root/utils/IdGenerator"
import type { EditionCreateRequest } from "@root/dtos/EditionCreateRequest"
import type { EditionUpdateRequest } from "@root/dtos/EditionUpdateRequest"
import { CoverSheetData } from "../_other/cover-sheet-data.svelte"
import { Optional } from "../_other/optional"
import type { ReadDraft } from "../types/read-draft"
import { ReadManageStore } from "./read-manage-store.svelte"
import { ReadDetailsStore } from "./read-record-details-store.svelte"



export type BookProfileStoreDependencies = {
    books:          BookRepository
    authors:        AuthorRepository
    universes:      UniverseRepository
    series:         SeriesRepository
    read_records:   ReadRecordRepository
    tags: TagRepository
    cover_repository: CoverRepository
    edition_repository: EditionRepository
}


export type BookDetails_Cover = {
    id:         string
    is_default: boolean
    resolution: string
    added_at:   string
    label:      string | null
}


export type BookProfile_EditionPreview = {
    id:           string
    label:        string
    release_date: string | null
    read_status:  string
    format:       EditionFileFormatDefinition
}


export type BookProfile_EditionDetails = {
    id:           string
    label:        string
    release_date: string | null
    read_status:  string
    file_size:    string
    added_at:     string
    format:       EditionFileFormatDefinition
}



export type EditionDraft2 = {
    local_id: string
    file:     File
    label:    string
    format:   string
    status:   "pending" | "uploading" | "failed"
    error:    string | null
    filename: string
}




export class BookProfileStore
{
    /*
    *   Blocks only on what the page cannot render without (book list, details). Read records failures are exposed through `read_records_error`.
    */
    public static async create(dependencies: BookProfileStoreDependencies, book_id: string): AsyncResult<BookProfileStore>
    {
        const [ details_result, ...other_results ] = await Promise.all([
            dependencies.books.details(book_id),
            dependencies.authors.ensure(),
            dependencies.universes.ensure(),
            dependencies.series.ensure(),
            dependencies.read_records.load(book_id),
            dependencies.tags.ensure(),
            dependencies.cover_repository.load(book_id),
            dependencies.edition_repository.load(book_id)
        ])

        if (!details_result.ok) 
        {
            return details_result
        }

        const failed_others = other_results.slice(0, 3).find(it => !it.ok)

        if (failed_others !== undefined && !failed_others.ok)
        {
            return failed_others
        }

        return { 
            ok: true, 
            value: new BookProfileStore(dependencies, details_result.value) 
        }
    }


    private book_details: BookDetailResponse

    private book:          BookDetailResponse   = $derived.by(() => this.book_details) // so TypeScript does not complain
    private book_repo:     BookRepository       = $derived.by(() => this.dependencies.books)
    private author_repo:   AuthorRepository     = $derived.by(() => this.dependencies.authors)
    private universe_repo: UniverseRepository   = $derived.by(() => this.dependencies.universes)
    private series_repo:   SeriesRepository     = $derived.by(() => this.dependencies.series)
    private read_repo:     ReadRecordRepository = $derived.by(() => this.dependencies.read_records)
    private tags_repo:     TagRepository        = $derived.by(() => this.dependencies.tags)
    private cover_repo:    CoverRepository      = $derived.by(() => this.dependencies.cover_repository)
    private edition_repo:  EditionRepository    = $derived.by(() => this.dependencies.edition_repository)

    public read_records:       ReadRecordResponse[]      = $derived(this.read_repo.get(this.book.id).sort((a, b) => -ReadRecordRules.compare(a, b)))
    public read_records_error: AppError | null           = $derived(this.read_repo.errorOf(this.book.id))
    public current_read:       ReadRecordResponse | null = $derived(ReadRecordRules.currentReadOf(this.read_records))
    public is_reading:         boolean                   = $derived(this.current_read !== null)
    public read_summary: ReadStatusSummary = $derived(ReadRecordRules.summaryOf(this.read_records))

    // DETAILS
    public book_id:     string           = $derived(this.book.id)
    public cover_id:    string | null    = $derived(this.book.cover_id)
    public title:       string           = $derived(this.book.title)
    public author:      string           = $derived(this.buildAuthorName() ?? "Unknown author")
    public universe:    string | null    = $derived(this.book.universe_id ? this.universe_repo.byId(this.book.universe_id)?.name ?? "Unknown universe" : null)
    public series:      string | null    = $derived(this.buildSeriesLabel())
    public thoughts:    string | null    = $derived(this.book.notes ? renderMarkdown(this.book.notes) : null)
    public blurb:       string | null    = $derived(this.book.blurb ? renderMarkdown(this.book.blurb) : null)
    public tags:        string[]         = $derived(this.book.tag_ids.map(it => this.tags_repo.byId(it)).map(it => it?.name ?? "error"))
    public released_at: string | null    = $derived(this.book.launch_date)
    public word_count:  string | null    = $derived(this.book.word_count ? `~ ${this.book.word_count} K` : null)
    public read_count:  string           = $derived(this.read_summary.kind === "dnf" ? "0 (DNF)" : this.read_summary.kind === "unread" ? "0" : this.read_summary.count.toString())

    // COVERS
    public covers:         CoverResponse[] = $derived(this.cover_repo.get(this.book.id))
    public covers_preview: BookDetails_Cover[] = $derived(this.covers.map(it => ({ id: it.id, is_default: it.is_default, resolution: "px", label: it.label, added_at: it.added_at }) ))
    public has_covers:     boolean         = $derived(this.covers.length > 0)

    public is_saving_cover: boolean       = $state(false)

    // READS
    public is_start_read_sheet_open: boolean = $state(false)
    public read_previews = $derived(this.read_repo.get(this.book.id).map(it => this.buildReadPreview(it)))
    public has_reads: boolean = $derived(this.read_previews.length > 0)

    public editions: EditionResponse[] = $derived(this.edition_repo.get(this.book.id))
    public pickable_editions: PickableEdition[] = $derived(this.editions.map(it => ({ id: it.id, format_id: it.format, label: it.label ?? "", added_at: it.added_at }) ))

    public start_date:        string        = $state("")
    public end_date:          string        = $state("")
    public is_dnf:            boolean       = $state(false)
    public is_historical:     boolean       = $state(false)
    public edition_id:        string | null = $derived(this.pickable_editions[0]?.id ?? null)
    public dont_have_edition: boolean       = $derived(this.edition_id ? false : true)


    // EDITIONS
    public has_editions:    boolean                      = $derived(this.editions.length > 0)
    public edition_preview: BookProfile_EditionPreview[] = $derived(this.editions.map(it => this.toEditionPreview(it)))

    public is_edition_details_sheet_open: boolean = $state(false)
    public inspected_edition_id:          string  = $state("")
    public edited_edition_id: string = $state("")
    
    public inspected_edition_details: BookProfile_EditionDetails = $derived(this.toEditionDetails(this.editions.find(it => it.id === this.inspected_edition_id) ?? null))

    public pending_edition: EditionDraft2 | null = $state(null)
    public is_edition_add_sheet_open: boolean = $state(false)
    public new_edition_label: string = $state("")
    public new_edition_release_date: string = $state("")
    public new_edition_filename: string = $derived(this.pending_edition?.filename ?? "")
    public is_uploading_edition: boolean = $state(false)
    public edition_upload_message: string = $state("")
    public is_edition_update_sheet_open: boolean = $state(false)
    public edition_update_message = $state("")



    //# manage covers
    public is_manage_cover_sheet_open: boolean             = $state(false)
    public manage_cover_sheet_data:    CoverSheetData.Type = $state(new CoverSheetData.Empty())
    
    public is_source_enabled:  boolean = $derived(!(this.manage_cover_sheet_data instanceof CoverSheetData.Create || this.manage_cover_sheet_data instanceof CoverSheetData.Update))

    public get manage_cover_sheet_label(): string
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "create":
            case "update": 
                return this.manage_cover_sheet_data.label.getOrElse("")
            
            case "empty":
            case "error":
            case "fetching": 
                return ""
        }
    }

    public set manage_cover_sheet_label(value: string)
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "create":
            case "update": 
                this.manage_cover_sheet_data = this.manage_cover_sheet_data.setLabel(Optional.from(value !== "" ? value : null))
                return
            
            case "empty":
            case "error":
            case "fetching": 
                return
        }
    }


    public get manage_cover_sheet_error(): string
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "error":
                return this.manage_cover_sheet_data.error

            case "create":
            case "update": 
            case "empty":
            case "fetching": 
                return ""
        }
    }

    public get manage_cover_sheet_cover_id(): string | null
    {
        switch (this.manage_cover_sheet_data.kind)
        {            
            case "update": 
               return this.manage_cover_sheet_data.cover_id
            
            case "create":
            case "empty":
            case "error":
            case "fetching": 
                return null
        }
    }


    public get manage_cover_sheet_file(): File | null
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "create":
                return this.manage_cover_sheet_data.file

            case "update": 
            case "empty":
            case "error":
            case "fetching": 
                return null
        }
    }

    public get manage_cover_sheet_resolution(): string | null
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "create":
            case "update": 
                return this.manage_cover_sheet_data.size
            
            case "empty":
            case "error":
            case "fetching": 
                return ""
        }
    }

    public get manage_cover_sheet_format(): string | null
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "create":
            case "update": 
                return this.manage_cover_sheet_data.format
            
            case "empty":
            case "error":
            case "fetching": 
                return ""
        }
    }

    public get is_manage_cover_button_enabled(): boolean
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "update": 
                console.log(!this.manage_cover_sheet_data.original_label.equals(this.manage_cover_sheet_data.label))
                return !this.manage_cover_sheet_data.original_label.equals(this.manage_cover_sheet_data.label)
            
            case "create":
            case "empty":
            case "error":
            case "fetching": 
                return true
        }
    }

    public async addFromUrl(url: string): Promise<void>
    {
        const trimmed_url = url.trim()

        if (trimmed_url === "")
        {
            this.manage_cover_sheet_data = new CoverSheetData.Error("the URL is empty")
            return
        }

        const result = await this.cover_repo.fetchFromUrl(trimmed_url)

        if (!result.ok)
        {
            this.manage_cover_sheet_data = new CoverSheetData.Error(`Could not get the image. ${describeError(result.error)}`)
            return
        }

        this.manage_cover_sheet_data = new CoverSheetData.Create(
            result.value,
            "?x? px",
            "IMG",
            Optional.none()
        )

        return
    }


    public addFromDevice(file: File): void
    {
        this.manage_cover_sheet_data = new CoverSheetData.Create(
            file,
            "?x? px",
            "IMG",
            Optional.none()
        )
    }

    public clearSheetData(): void
    {
        this.manage_cover_sheet_data = new CoverSheetData.Empty()
    }

    public async finishManageCover(): Promise<void>
    {
        switch (this.manage_cover_sheet_data.kind)
        {
            case "create":
                const create_result = await this.cover_repo.create({
                    book_id: this.book_id,
                    label:   this.manage_cover_sheet_data.label.getOrNull(),
                    file:    this.manage_cover_sheet_data.file,
                })

                if (!create_result.ok)
                {
                    this.manage_cover_sheet_data = new CoverSheetData.Error(`Could not upload: ${describeError(create_result.error)}`)
                    return
                }

                this.manage_cover_sheet_data = new CoverSheetData.Empty()
                this.is_manage_cover_sheet_open = false

                return
            case "update": 
                const update_result = await this.cover_repo.update(
                    this.manage_cover_sheet_data.cover_id,
                    {
                        label: this.manage_cover_sheet_data.label.getOrNull(),
                    }
                )

                if (!update_result.ok)
                {
                    this.manage_cover_sheet_data = new CoverSheetData.Error(`Could not update: ${describeError(update_result.error)}`)
                }

                this.manage_cover_sheet_data = new CoverSheetData.Empty()
                this.is_manage_cover_sheet_open = false

                return
            
            case "empty":
            case "error":
            case "fetching": 
                return
        }
    }

    public triggerCoverEditSheet(id: string)
    {
        const cover_data = this.covers.find(cover => cover.id === id)

        if (!cover_data) return

        this.manage_cover_sheet_data = new CoverSheetData.Update(
            cover_data.id,
            Optional.from(cover_data.label),
            Optional.from(cover_data.label),
            "?x? px",
            "IMG"
        )
        this.is_manage_cover_sheet_open = true
    }

    public async deleteCover(id: string)
    {
        const cover_data = this.covers.find(cover => cover.id === id)

        if (!cover_data) return

        const result = await this.cover_repo.delete(cover_data)

        if (!result.ok)
        {
            this.manage_cover_sheet_data = new CoverSheetData.Error(`Failed to delete cover: ${describeError(result.error)}`)
            return
        }

        this.manage_cover_sheet_data = new CoverSheetData.Empty()
        this.is_manage_cover_sheet_open = false
    }





    public read_manage_store:  ReadManageStore
    public read_details_store: ReadDetailsStore


    public editRead(read_id: string)
    {
        this.read_manage_store.is_open = false
        this.read_details_store.is_open = false

        this.read_manage_store.editExisting(read_id)
    }


    public async deleteRead(read_id: string): Promise<boolean>
    {
        const reads = this.read_repo.get(this.book_id)
        const read_target = reads.find(read => read.id === read_id)

        if (!read_target)
        {
            return false
        }

        const result = await this.read_repo.delete(read_target)

        if (result.ok)
        {
            this.read_details_store.is_open = false
            return true
        }

        return false
    }



    private constructor(
        private dependencies: BookProfileStoreDependencies, 
        details: BookDetailResponse,
    ) 
    {
        this.book_details = $state.raw(details)

        this.read_manage_store = new ReadManageStore(dependencies.edition_repository, dependencies.read_records, details.id)
        this.read_details_store   = new ReadDetailsStore(dependencies.read_records, dependencies.edition_repository, details.id)
    }


    public async setDefaultCover(cover_id: string): Promise<void>
    {
        if (this.is_saving_cover || cover_id === this.book.cover_id) return 

        this.is_saving_cover = true

        const result = await this.book_repo.update(this.book.id, { cover_id: cover_id })

        this.is_saving_cover = false

        if (!result.ok) return

        this.cover_repo.markDefault(this.book.id, cover_id)
        this.book_details = { ...this.book_details, cover_id: result.value.cover_id }
    }


    public async refresh(): Promise<void>
    {
        this.dependencies.read_records.invalidate(this.book.id)

        await Promise.all([
            this.dependencies.read_records.load(this.book.id),
            this.loadDetails(),
        ])
    }


    private async loadDetails(): AsyncResult<BookDetailResponse>
    {
        const result = await this.book_repo.details(this.book.id)

        if (result.ok)
        {
            this.book = result.value
        }

        return result
    }


    private buildAuthorName(): string | null
    {
        const author = this.author_repo.byId(this.book.author_id)

        if (author === undefined)
        {
            return null
        }

        return AuthorRules.nameOf(author)
    }


    /*
    * Non-breaking space keeps "#1" on the same line as the last word of the series name.
    */
    private buildSeriesLabel(): string | null
    {
        const series = this.series_repo.byId(this.book.series_id)

        if (series === undefined) 
        {
            return null
        }

        const volume_number = this.book.volume_number

        if (volume_number === null)
        {
            return series.name
        }

        return `${series.name}\u00A0#${volume_number}`
    }


    private buildReadPreview(read: ReadRecordResponse)
    {
        const edition: EditionResponse | null = read.edition_id ? this.editions.find(it => it.id === read.edition_id) ?? null : null
        const format_definition = edition_format_color_registry.resolve(edition?.format ?? EditionFormatColorRegistry.UNKNOWN)
        const rendered_note = read.notes ? renderMarkdown(read.notes) : null
        
        return {
            id: read.id,
            format_definition: format_definition,
            rendered_note: rendered_note,
            start_date: read.start_date,
            end_date: read.end_date,
            is_in_progress: !ReadRecordRules.isFinished(read),
            is_historical: read.is_historical,
        }
    }


    public async handleReadData(save: boolean): Promise<boolean>
    {
        if (!save)
        {
            this.start_date = ""
            this.end_date = ""
            this.is_dnf = false
            this.is_historical = false

            console.log("dumped data")
            return true
        }

        const read_request: ReadRecordCreateRequest = {
            book_id: this.book.id,
            edition_id: this.edition_id,
            start_date: this.start_date !== "" ? this.start_date : null,
            end_date: this.end_date !== "" ? this.end_date : null,
            did_not_finish: this.is_dnf,
            is_historical: this.is_historical,
            stop_position: null,
            notes: null,
        }

        const result = await this.read_repo.create(read_request)

        if (!result.ok)
        {
            return false
        }

        this.start_date = ""
        this.end_date = ""
        this.is_dnf = false
        this.is_historical = false
        this.dont_have_edition = false

        return true
    }


    public async finishRead(read_id: string): Promise<boolean>
    {
        const read = this.read_repo.get(read_id)

        if (!read) return false

        const request: ReadRecordUpdateRequest = {
            end_date: new Date().toISOString().slice(0, 10)
        }

        const result = await this.read_repo.update(read_id, request)

        return result.ok
    }

    private getReadStatusMessage(edition: EditionResponse): string
    {
        const book_reads = this.read_repo.get(this.book_id)
        const edition_reads = book_reads.filter(it => it.edition_id === edition.id)
        const read_summary: ReadStatusSummary = ReadRecordRules.summaryOf(edition_reads)

        return read_summary.kind === "finished" 
            ? read_summary.count > 1 
                ? `Read (${read_summary.count})` 
                : "Read"
            : read_summary.kind === "dnf"
            ? "DNF"
            : "In progress"
    }


    private toEditionPreview(edition: EditionResponse): BookProfile_EditionPreview
    {
        const read_status_messge = this.getReadStatusMessage(edition)
        const format_definition = edition_format_color_registry.resolve(edition.format)

        return {
            id: edition.id,
            label: edition.label ?? "",
            release_date: edition.release_date,
            read_status: read_status_messge,
            format: format_definition,
        }
    }


    private toEditionDetails(edition: EditionResponse | null): BookProfile_EditionDetails
    {
        if (!edition)
        {
            return {
                id: "",
                label: "",
                release_date: "",
                read_status: "",
                file_size: "",
                added_at: "",
                format: edition_format_color_registry.resolve(EditionFormatColorRegistry.UNKNOWN)
            }
        }

        const preview = this.toEditionPreview(edition)

        return {
            ...preview,
            file_size: "",
            added_at: edition.added_at
        }
    }


    public async deleteInspectedEdition(): Promise<boolean>
    {
        const edition = this.editions.find(it => it.id === this.inspected_edition_id)

        if (!edition) return false

        const result = await this.edition_repo.delete(edition)

        if (result.ok)
        {
            this.book_repo.invalidate()
            void this.book_repo.load()

            this.read_repo.invalidate(this.book.id)
            void this.read_repo.load(this.book.id)

            const book = await this.book_repo.details(this.book.id)

            if (!book.ok) return false

            this.book_details = book.value
        }

        return result.ok
    }


    public stageFile(file: File)
    {
        this.pending_edition = {
            local_id: generateEphemeralId(),
            file:     file,
            label:    "",
            format:   this.formatOf(file),
            status:   "pending",
            error:    null,
            filename: file.name
        }

        console.log(this.pending_edition)
    }

    private formatOf(file: File): string
    {
        const parts = file.name.split(".")

        if (parts.length < 2)
        {
            return "unknown"
        }

        return parts.at(-1)!.toLowerCase()
    }


    public async uploadDrafts(): Promise<void>
    {
        if (this.is_uploading_edition || !this.pending_edition)
        {
            return
        }

        console.log("uploading...")

        this.is_uploading_edition  = true

        const edition_create_request: EditionCreateRequest = {
            book_id: this.book_id,
            label: this.new_edition_label,
            release_date: this.new_edition_release_date,
            format: this.pending_edition.format,
            file: this.pending_edition.file
        }

        const result = await this.edition_repo.create(edition_create_request)

        if (!result.ok)
        {
            this.edition_upload_message = "Failed to upload the file."
        }
        else
        {
            this.is_edition_add_sheet_open = false

            this.pending_edition = null
            this.new_edition_label = ""
            this.new_edition_release_date = ""
            this.edition_upload_message = ""
        }

        this.is_uploading_edition = false
    }
    

    public editEdition(id: string)
    {
        const edition = this.editions.find(it => it.id === id)

        if (!edition) return

        this.new_edition_label = edition.label ?? ""
        this.new_edition_release_date = edition.release_date ?? ""

        this.is_edition_details_sheet_open = false
        this.is_edition_update_sheet_open = true
    }


    public async updateEdition()
    {
        console.log(this.inspected_edition_id)

        if (this.inspected_edition_id === "") return

        console.log("updating...")

        const update_request: EditionUpdateRequest = {
            label: this.new_edition_label !== "" ? this.new_edition_label : undefined,
            release_date: this.new_edition_release_date !== "" ? new Date(this.new_edition_release_date).toISOString().slice(0, 10) : undefined
        }

        console.log(update_request)

        const result = await this.edition_repo.update(this.inspected_edition_id, update_request)

        if (!result.ok)
        {
            this.edition_update_message = "Could not update edition"
        }
        else
        {
            this.edition_update_message = ""
            this.new_edition_label = ""
            this.new_edition_release_date = ""

            this.is_edition_update_sheet_open = false
            this.is_edition_details_sheet_open = true
        }
    }
}