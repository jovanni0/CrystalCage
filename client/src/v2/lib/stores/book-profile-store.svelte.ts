import type { BookDetailResponse } from "@root/dtos/BookDetailResponse"
import type { AuthorRepository } from "../repositories/author-repository.svelte"
import type { BookRepository } from "../repositories/book-repository.svelte"
import type { ReadRecordRepository } from "../repositories/read-record-repository.svelte"
import type { SeriesRepository } from "../repositories/series-repository.svelte"
import type { UniverseRepository } from "../repositories/universe-repository.svelte"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import type { AppError, AsyncResult } from "../utils/result"
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
    
    public inspected_edition_details: BookProfile_EditionDetails = $derived(this.toEditionDetails(this.editions.find(it => it.id === this.inspected_edition_id) ?? null))



    private constructor(
        private dependencies: BookProfileStoreDependencies, 
        details: BookDetailResponse,
    ) 
    {
        this.book_details = $state.raw(details)
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
}