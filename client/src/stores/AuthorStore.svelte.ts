import type { AuthorResponse }      from "@root/dtos/AuthorResponse";
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse";
import type { SeriesResponse }      from "@root/dtos/SeriesResponse";
import type { UniverseResponse }    from "@root/dtos/UniverseResponse";
import type { Logger }              from "@root/loggers/Logger";
import type { BooksBySeries }       from "@root/types/BooksAsSeries";
import type { ApiClient }           from "@root/utils/ApiClient";
import { formatAuthorName, groupBySeries } from "@root/utils/LibraryGroupings";



export class AuthorStore
{
    private author:    AuthorResponse | null = $state(null)
    private books:     BookSummaryResponse[] = $state([])
    private universes: UniverseResponse[]    = $state([])
    private series:    SeriesResponse[]      = $state([])

    public author_name: string                = $derived(this.author ? formatAuthorName(this.author) : "")
    public author_id:   string                = $derived(this.author?.id ?? "")
    public gallery:     BookSummaryResponse[] = $derived(this.books)
    public by_series:   BooksBySeries[]       = $derived(groupBySeries(this.books, this.universes, this.series))


    constructor(id: string, private logger: Logger, private api: ApiClient)
    {
        this.fetchAll(id)
    }


    private async fetchAll(id: string)
    {
        const [author, universes, series, books] = await Promise.all([
            this.api.getAuthorById(id),
            this.api.getUniverses(id),
            this.api.getSeries(id, null),
            this.api.getBooks(id),
        ])

        this.author = author
        this.universes = universes
        this.series = series
        this.books = books
    }
}