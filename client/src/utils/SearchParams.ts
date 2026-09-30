


export function toSearchParams(data: Record<string, string | null>): string
{
    const search_params = new URLSearchParams()

    for (const [key, value] of Object.entries(data))
    {
        if (value === null)
        {
            continue
        }

        search_params.set(key, value)
    }

    return search_params.toString()
}