import type { TagResponse } from "../dtos/tag.dto"



export class TagQuery
{
    public filter(tags: TagResponse[], query: string): TagResponse[]
    {
        const query_lowercase = query.toLowerCase().trim()

        if (query === "")
        {
            return tags
        }

        return tags.filter(it => it.name.toLowerCase().includes(query_lowercase))
    }


    public nameOfId(tags: TagResponse[], id: string | null): string | null
    {
        if (!id)
        {
            return null
        }
        
        const target = tags.find(it => it.id === id)

        if (!target)
        {
            return null
        }

        return target.name
    }
}