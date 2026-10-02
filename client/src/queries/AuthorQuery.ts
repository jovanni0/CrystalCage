import type { AuthorResponse } from "@root/dtos/AuthorResponse";



export class AuthorQuery
{
    public filter(authors: AuthorResponse[], query: string): AuthorResponse[]
    {
        const query_lowercase = query.toLowerCase().trim()

        if (query === "")
        {
            return authors
        }

        return authors.filter(it => this.nameOf(it).toLowerCase().includes(query_lowercase))
    }


    public nameOfId(authors: AuthorResponse[], id: string | null): string | null
    {
        if (!id)
        {
            return null
        }

        const target = authors.find(it => it.id === id)

        if (!target)
        {
            return null
        }

        return this.nameOf(target)
    }


    public nameOf(author: AuthorResponse)
    {
        if (author.first_name)
        {
            return `${author.last_name}, ${author.first_name}`
        }
        
        return author.last_name
    }


    /**
     * Splits a name info First and Last name, based on the following 2 cases:
     * - "Last, First": everything before the first comma is the last name.
     * - "First Middle Last": the last word is the last name.
     */
    public splitName(full_name: string): { first_name: string | null, last_name: string }
    {
        if (full_name.includes(","))
        {
            const [last, ...rest] = full_name.split(",").map(it => it.trim())

            return { 
                first_name: rest.join(", ") || null, 
                last_name: last 
            }
        }

        const words = full_name.trim().split(/\s+/).filter(Boolean)
        const last  = words.pop() ?? ""

        return { 
            first_name: words.join(" ") || null, 
            last_name: last 
        }
    }


    /**
     * Interprets the search text and returns the author name (as Last, First) if there is no exact match, otherwise `undefined`.
     */
    public getCreatableAuthorName(search: string, authors: AuthorResponse[]): string | undefined
    {
        const name_from_search = this.splitName(search)
        const target = authors.find(it => it.first_name === name_from_search.first_name && it.last_name === name_from_search.last_name)
        
        if (target)
        {
            return undefined
        }

        if (name_from_search.first_name)
        {
            return `${name_from_search.last_name}, ${name_from_search.first_name}`
        }
        
        return name_from_search.last_name
    }
}