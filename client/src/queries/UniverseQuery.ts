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


    public nameOf(universes: UniverseResponse[], id: string | null): string | null
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
}