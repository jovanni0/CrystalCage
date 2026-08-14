import type { AuthorResponse }      from "@root/dtos/AuthorResponse";
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";
import type { SeriesResponse }      from "@root/dtos/SeriesResponse";
import type { UniverseResponse }    from "@root/dtos/UniverseResponse";
import type { Logger }              from "@root/loggers/Logger";
import type { LibraryByAuthor }     from "@root/types/LibraryByAuthor";
import type { LibraryBySeries }     from "@root/types/LibraryBySeries";
import type { LibraryItem }         from "@root/types/LibraryItem";
import type { ApiClient }           from "@root/utils/ApiClient";
import { groupAsSeries, groupByAuthor } from "@root/utils/LibraryGroupings";



export class LibraryStore
{
    private books:     BookSummaryResponse[] = $state([])
    private authors:   AuthorResponse[]      = $state([])
    private universes: UniverseResponse[]    = $state([])
    private series:    SeriesResponse[]      = $state([])

    public gallery:   LibraryItem[]     = $derived(this.books.map(it => ({ name: it.title, cover_id: it.cover_id, number_of_volumes: 0, id: it.id })))
    public by_author: LibraryByAuthor[] = $derived(groupByAuthor(this.authors, this.books))
    public by_series: LibraryBySeries[] = $derived(groupAsSeries(this.authors, this.universes, this.series, this.books))

    public is_library_empty: boolean = $derived(this.books.length === 0)

    constructor(
        private logger: Logger,
        private api: ApiClient,
    )
    {}


    public async initialize()
    {
        this.logger.info("initializing library store")

        await Promise.all([
            this.fetchBooks(),
            this.fetchAuthors(),
            this.fetchUniverses(),
            this.fetchSeries()
        ])

        this.logger.info("finished initializing library store")
    }


    private async fetchBooks()
    {
        this.books = await this.api.getBooks(null)
    }


    private async fetchAuthors()
    {
        this.authors = await this.api.getAuthors()
    }


    private async fetchUniverses(author_id: string | null = null): Promise<void>
    {
        this.universes = await this.api.getUniverses(author_id)
    }


    private async fetchSeries(author_id: string | null = null, universe_id: string | null = null): Promise<void>
    {
        this.series = await this.api.getSeries(author_id, universe_id)
    }
}   