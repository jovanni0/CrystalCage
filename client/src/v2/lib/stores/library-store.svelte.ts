import type { AsyncResult }            from "../utils/result"
import type { BookSummaryResponse }    from "@root/dtos/BookSummaryResponse"
import type { ReadRecordResponse }     from "@root/dtos/ReadRecordResponse"
import type { AuthorRepository }       from "../repositories/author-repository.svelte"
import type { BookRepository }         from "../repositories/book-repository.svelte"
import type { EditionRepository }      from "../repositories/edition-repository.svelte"
import type { ReadRecordRepository }   from "../repositories/read-record-repository.svelte"
import type { ReadProgressRepository } from "../repositories/read-progress-repository.svelte"
import      { AuthorRules }            from "../rules/author-rules"
import      { ReadRecordRules }        from "../rules/read-record-rules"
import      { ReadProgressRules }      from "../rules/read-progress-rules"
import      { describeError }          from "../utils/result"



export type LibraryItem = {
    book_id:  string
    cover_id: string | null
    label:    string
}


export type CurrentRead = {
    book_id:        string
    read_record_id: string
    cover_id:       string | null
    title:          string
    author:         string
    progress:       number
    format:         string | null
    edition_label:  string | null
}


export type LibraryStoreDependencies = {
    books:         BookRepository
    authors:       AuthorRepository
    editions:      EditionRepository
    read_records:  ReadRecordRepository
    read_progress: ReadProgressRepository
}



export class LibStore
{
    public is_progress_sheet_open: boolean       = $state(false)
    public is_saving_progress:     boolean       = $state(false)
    public error_message:          string | null = $state(null)

    private selected_read_id: string | null = $state(null)

    public items: LibraryItem[] = $derived(
        this.dependencies.books.all.map(it => ({ book_id: it.id, cover_id: it.cover_id, label: it.title }))
    )

    public current_reads: CurrentRead[] = $derived(this.buildCurrentReads())
    public has_current_reads: boolean = $derived(this.current_reads.length > 0)

    public selected_read: CurrentRead | null = $derived(
        this.current_reads.find(it => it.read_record_id === this.selected_read_id) ?? null
    )


    private constructor(private dependencies: LibraryStoreDependencies)
    {}


    public static async create(dependencies: LibraryStoreDependencies): AsyncResult<LibStore>
    {
        const store = new LibStore(dependencies)

        const base_results = await Promise.all([
            dependencies.books.load(),
            dependencies.authors.load(),
        ])

        const failed = base_results.find(it => !it.ok)

        if (failed !== undefined && !failed.ok)
        {
            return failed
        }

        await store.loadCurrentReads()

        return { ok: true, value: store }
    }


    public openProgressSheet(read_record_id: string): void
    {
        this.selected_read_id       = read_record_id
        this.error_message          = null
        this.is_progress_sheet_open = true
    }


    public async saveProgress(progress: number): Promise<void>
    {
        const read = this.selected_read

        if (read === null)
        {
            return
        }

        this.is_saving_progress = true

        const result = await this.dependencies.read_progress.create({ read_record_id: read.read_record_id, progress })

        this.is_saving_progress = false

        if (!result.ok)
        {
            this.error_message = `Could not save progress. ${describeError(result.error)}`
            return
        }

        this.is_progress_sheet_open = false
    }


    /*
    * Per-book data for the carousel. Failures here are logged by the repositories but do not block the library.
    */
    private async loadCurrentReads(): Promise<void>
    {
        const in_progress = this.inProgressBooks()

        await Promise.all(in_progress.flatMap(book => [
            this.dependencies.read_records.load(book.id),
            this.dependencies.editions.load(book.id),
        ]))

        const read_ids = in_progress
            .map(book => ReadRecordRules.currentReadOf(this.dependencies.read_records.get(book.id)))
            .filter(it => it !== null)
            .map(it => it.id)

        await Promise.all(read_ids.map(id => this.dependencies.read_progress.load(id)))
    }


    private inProgressBooks(): BookSummaryResponse[]
    {
        return this.dependencies.books.all.filter(it => it.read_status === "IN_PROGRESS")
    }


    private buildCurrentReads(): CurrentRead[]
    {
        const reads: CurrentRead[] = []

        for (const book of this.inProgressBooks())
        {
            const current_read = ReadRecordRules.currentReadOf(this.dependencies.read_records.get(book.id))

            if (current_read === null)
            {
                continue
            }

            reads.push(this.toCurrentRead(book, current_read))
        }

        return reads
    }


    private toCurrentRead(book: BookSummaryResponse, read: ReadRecordResponse): CurrentRead
    {
        const author  = this.dependencies.authors.byId(book.author_id)
        const edition = this.dependencies.editions.get(book.id).find(it => it.id === read.edition_id)
        const latest  = ReadProgressRules.latestOf(this.dependencies.read_progress.get(read.id))

        return {
            book_id:        book.id,
            read_record_id: read.id,
            cover_id:       book.cover_id,
            title:          book.title,
            author:         author ? AuthorRules.nameOf(author) : "Unknown author",
            progress:       latest?.progress ?? 0,
            format:         edition?.format ?? null,
            edition_label:  edition?.label ?? null,
        }
    }
}