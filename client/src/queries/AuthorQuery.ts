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


    public splitName(full_name: string): { first_name: string | null, last_name: string }
    {
        if (full_name.includes(','))
        {
            const parts = full_name.split(',').map(it => it.trim())
            const last_name = parts[0]
            const first_name = parts.slice(1).join(', ')

            return {
                first_name: first_name || null,
                last_name: last_name ?? ""
            }
        }
        else
        {
            const parts = full_name.split(' ').map(it => it.trim())

            if (parts.length > 1)
            {
                const last_name = parts[0]
                const first_name = parts.slice(1).join(', ')
    
                return {
                    first_name: first_name || null,
                    last_name: last_name ?? ""
                }
            }
            
            return {
                first_name: null,
                last_name: parts[0]
            }
        }
    }
}