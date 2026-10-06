import type { TagCreateRequest } from "@root/dtos/TagCreateRequest"
import type { TagCategoryResponse } from "../dtos/tag-category.dto"
import type { TagResponse } from "../dtos/tag.dto"



export class TagRules
{
    public static filterByQuery(tags: TagResponse[], query: string): TagResponse[]
    {
        const query_lowercase = query.toLowerCase().trim()

        if (query === "")
        {
            return tags
        }

        return tags.filter(it => it.name.toLowerCase().includes(query_lowercase))
    }


    public static nameOfId(tags: TagResponse[], id: string | null): string | null
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


    /**
     * Groupes tags by category.
     */
    public static groupTags(tags: TagResponse[], categories: TagCategoryResponse[]): { id: string, name: string, tags: TagResponse[] }[]
    {
        const unique_categories = new Set(categories.map(it => it.id))

        // tags with no category, or a category that no longer exists, go to the fallback group
        const by_category = Map.groupBy(
            tags,
            tag => tag.category_id && unique_categories.has(tag.category_id) ? tag.category_id : null
        )

        const groups = categories.map(category => ({
            id:   category.id,
            name: category.name,
            tags: by_category.get(category.id) ?? [],
        }))

        groups.push({ 
            id: "uncategorized", 
            name: "", 
            tags: by_category.get(null) ?? [] 
        })

        groups.sort((a, b) => a.name.localeCompare(b.name))

        return groups.filter(group => group.tags.length > 0)
    }


    public static toCreateRequest(name: string, description: string = ""): TagCreateRequest
    {
        return {
            name: name.trim(),
            category_id: null,
            description
        }
    }
}