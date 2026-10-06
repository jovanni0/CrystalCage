import type { UniverseResponse } from "@root/dtos/UniverseResponse"



export class UniverseQuery
{
    public filter(universes: UniverseResponse[], query: string): UniverseResponse[]
    {
        const query_lowercase = query.toLowerCase().trim()

        if (query === "")
        {
            return universes
        }

        return universes.filter(it => it.name.toLowerCase().includes(query_lowercase))
    }


    public nameOfId(universes: UniverseResponse[], id: string | null): string | null
    {
        if (!id)
        {
            return null
        }
        
        const target = universes.find(it => it.id === id)

        if (!target)
        {
            return null
        }

        return target.name
    }

    /**
     * Returns the new universe name if there is no exact match with the search.
     */
    public getCreatableUniverse(search: string, universes: UniverseResponse[]): string | undefined
    {
        const universe_from_search = search.trim()
        const target = universes.find(it => it.name === universe_from_search)

        if (!target)
        {
            return universe_from_search
        }

        return undefined
    }
}