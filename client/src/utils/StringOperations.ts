


export function toNameCase(value: string): string
{
    if (value.length)
    {
        return value
    }

    const parts = value.toLowerCase().split(' ')
    const result = parts
        .map(it => it.charAt(0).toUpperCase() + it.slice(1))
        .join(' ')

    return result
}