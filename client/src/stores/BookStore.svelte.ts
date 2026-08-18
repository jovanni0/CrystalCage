import type { AuthorResponse } from "@root/dtos/AuthorResponse"
import type { BookDetailResponse } from "@root/dtos/BookDetailResponse"
import type { CoverResponse } from "@root/dtos/CoverResponse"
import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse"
import type { SeriesResponse } from "@root/dtos/SeriesResponse"
import type { TagResponse } from "@root/dtos/TagResponse"
import type { UniverseResponse } from "@root/dtos/UniverseResponse"
import type { Logger } from "@root/loggers/Logger"
import type { ReadStatus } from "@root/types/ReadStatus"
import type { ApiClient } from "@root/utils/ApiClient"
import { TagQuery } from "@root/queries/TagQuery"
import { buildReadHistory, formatAuthorName } from "@root/utils/LibraryGroupings"
import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { ReadHistoryItem } from "@root/types/ReadHistoryItem"
import type { ReadRecordCreateRequest } from "@root/dtos/ReadRecordCreateRequest"
import { getIntervalInDays } from "@root/utils/ReadRecordUtils"



export class BookStore
{
    private tag_query: TagQuery = new TagQuery()

    private author:   AuthorResponse | null     = $state(null)
    private universe: UniverseResponse | null   = $state(null)
    private series:   SeriesResponse | null     = $state(null)
    private book:     BookDetailResponse | null = $state(null)
    private reads:    ReadRecordResponse[]      = $state([])
    private tags:     TagResponse[]             = $state([])

    public editions: EditionResponse[]         = $state([])
    public covers: CoverResponse[] = $state([])

    public author_id:       string            = $derived(this.author?.id ?? "")
    public author_name:     string            = $derived(this.author !== null ? formatAuthorName(this.author) : "")
    public universe_name:   string            = $derived(this.universe?.name ?? "")
    public series_name:     string            = $derived(this.series?.name ?? "")
    public title:           string            = $derived(this.book?.title ?? "")
    public volume_number:   number | null     = $derived(this.book?.volume_number ?? null)
    public launch_date:     string | null     = $derived(this.book?.launch_date ?? null)
    public word_count:      number | null     = $derived(this.book?.word_count ?? null)
    public blurb:           string | null     = $derived(this.book?.blurb ?? null)
    public notes:           string | null     = $derived(this.book?.notes ?? null)
    public tag_names:       string[]          = $derived(this.book?.tag_ids.map(it => this.tag_query.nameOf(this.tags, it)).filter(it => it !== null) ?? [])
    public read_status:     ReadStatus        = $derived(this.book?.read_status ?? "UNREAD")
    public read_count:      number            = $derived(this.book?.reread_count ?? 0)
    public read_history:    ReadHistoryItem[] = $derived(buildReadHistory(this.reads, this.editions))
    public cover_id:        string | null     = $derived(this.book?.cover_id ?? null)
    public total_read_time: number            = $derived(this.reads.map(it => getIntervalInDays(it.start_date, it.end_date)).filter(it => it !== null).reduce( (total, it) => total + it, 0))

    public can_start_read: boolean = $derived(this.reads.find(it => it.start_date !== null && it.end_date === null) === undefined)


    constructor(public book_id: string, private logger: Logger, private api: ApiClient)
    {
        this.fetchAll(book_id)
    }


    private async fetchAll(id: string)
    {           
        try
        {
            this.book = await this.api.getBook(id)

            const [series, universe, author, tags, covers, reads, editions] = await Promise.all([
                this.book.series_id   !== null ? this.fetchSeries(this.book.author_id, this.book.universe_id, this.book.series_id) : Promise.resolve(null),
                this.book.universe_id !== null ? this.fetchUniverse(this.book.author_id, this.book.universe_id) : Promise.resolve(null),
                this.api.getAuthorById(this.book.author_id),
                this.api.getTags(),
                this.api.getCovers(this.book.id),
                this.api.getReadRecords(this.book.id),
                this.api.getEditions(this.book.id),
            ])
            
            this.series   = series
            this.universe = universe
            this.author   = author
            this.tags     = tags
            this.covers   = covers
            this.reads    = reads
            this.editions = editions

        }
        catch (e)
        {
            this.logger.error("error trying to fetch series data", { error: e })
        }
    }


    private async fetchSeries(author_id: string, universe_id: string | null, series_id: string): Promise<SeriesResponse>
    {
        const series = (await this.api.getSeries(author_id, universe_id))
            .filter(it => it.id === series_id)
            .at(0)

        if (series === undefined)
        {
            throw Error("did not get any series back from the server")
        }

        return series
    }


    private async fetchUniverse(author_id: string, universe_id: string): Promise<UniverseResponse>
    {
        const universe = (await this.api.getUniverses(author_id))
            .filter(it => it.id === universe_id)
            .at(0)

        if (universe === undefined)
        {
            throw Error("did not get any universes back from the server")
        }

        return universe
    }


    public async setCoverAsDefault(id: string)
    {
        const book = await this.api.updateBook(this.book_id, { cover_id: id })
        this.covers = await this.api.getCovers(this.book_id)

        if (this.book)
        {
            this.book.cover_id = book.cover_id
        }
    }


    public async startRead(edition_id: string): Promise<void>
    {
        if (!this.can_start_read)
        {
            this.logger.error("another read is alredy started")
            return
        }

        const read_record: ReadRecordCreateRequest = {
            book_id: this.book_id,
            edition_id: edition_id,
            start_date: new Date().toISOString().slice(0, 10),
            end_date: null,
            did_not_finish: false,
            is_historical: false,
            stop_position: null,
            notes: null
        }

        try
        {
            const record = await this.api.createReadRecord(read_record)
            
            this.reads = await this.api.getReadRecords(this.book_id)
            this.book = await this.api.getBook(this.book_id)

            this.logger.info("created a new read record", { record: record })
        }
        catch (e)
        {
            this.logger.error("failed to create a new record", { error: e })
        }

    }


    public async finishRead(id: string)
    {
        await this.api.updateReadRecord(id, { end_date: new Date().toISOString().slice(0, 10) })
        
        this.reads = await this.api.getReadRecords(this.book_id)
        this.book = await this.api.getBook(this.book_id)
    }
}