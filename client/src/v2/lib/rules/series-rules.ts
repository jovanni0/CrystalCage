import type { SeriesCreateRequest } from "@root/dtos/SeriesCreateRequest"
import type { SeriesResponse } from "@root/dtos/SeriesResponse"



export class SeriesRules
{
    public static filterByQuery(series: SeriesResponse[], query: string): SeriesResponse[]
    {
        const query_lowercase = query.toLowerCase().trim()

        if (query === "")
        {
            return series
        }

        return series.filter(it => it.name.toLowerCase().includes(query_lowercase))
    }


    public static nameOf(series: SeriesResponse[], id: string | null): string | null
    {
        if (!id)
        {
            return null
        }
        
        const target = series.find(it => it.id === id)

        if (!target)
        {
            return null
        }

        return target.name
    }


    /**
     * Returns the new series name if there is no exact match with the search.
     */
    public static getCreatableSeries(search: string, series: SeriesResponse[]): string | undefined
    {
        const series_from_search = search.trim()
        const target = series.find(it => it.name === series_from_search)

        if (!target)
        {
            return series_from_search
        }

        return undefined
    }


    public static toCreateRequest(author_id: string, universe_id: string | null, name: string, is_finished: boolean = false): SeriesCreateRequest
    {
        return {
            author_id,
            universe_id,
            name: name.trim(),
            is_finished
        }
    }
}