import type { SeriesResponse } from "@root/dtos/SeriesResponse"



export class SeriesQuery
{
    public filter(series: SeriesResponse[], query: string): SeriesResponse[]
    {
        const query_lowercase = query.toLowerCase().trim()

        if (query === "")
        {
            return series
        }

        return series.filter(it => it.name.toLowerCase().includes(query_lowercase))
    }


    public nameOf(series: SeriesResponse[], id: string | null): string | null
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
}