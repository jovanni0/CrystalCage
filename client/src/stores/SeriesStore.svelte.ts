import type { AuthorResponse }       from "@root/dtos/AuthorResponse";
import type { SeriesBookItem }       from "@root/dtos/SeriesBookItem";
import type { SeriesDetailResponse } from "@root/dtos/SeriesDetailResponse";
import type { Logger }               from "@root/loggers/Logger";
import type { ApiClient }            from "@root/utils/ApiClient";
import { formatAuthorName }          from "@root/utils/LibraryGroupings";



export class SeriesStore
{
    private author: AuthorResponse | null       = $state(null)
    private series: SeriesDetailResponse | null = $state(null)

    public books:       SeriesBookItem[] = $derived(this.series?.books ?? [])
    public series_name: string           = $derived(this.series?.name ?? "")
    public author_name: string           = $derived(this.author !== null ? formatAuthorName(this.author) : "")
    public author_id:   string | null    = $derived(this.author?.id ?? null)


    constructor(id: string, private api: ApiClient, private logger: Logger)
    {
        this.fetchAll(id)
    }


    private async fetchAll(series_id: string)
    {
        try
        {
            this.series = await this.api.getSeriesById(series_id)
            this.author = await this.api.getAuthorById(this.series.author_id)
        }
        catch (e)
        {
            this.logger.error("error trying to fetch series data", { error: e })
        }
    }
}