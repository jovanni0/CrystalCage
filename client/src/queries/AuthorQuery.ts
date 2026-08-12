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

        return authors.filter(it => `${it.last_name, it.first_name ?? ""}`.toLowerCase().includes(query_lowercase))
    }


    public nameOf(authors: AuthorResponse[], id: string | null): string | null
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

        return `${target.last_name}, ${target.first_name}`
    }


    public splitName(full_name: string): {first_name: string | null, last_name: string}
    {
        const [last_name, first_name] = full_name.split(",").map(it => it.trim())

        return {
            first_name: first_name || null,
            last_name: last_name ?? ""
        }
    }
}