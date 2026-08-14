


export function formatColor(format: string): string
{
    const colors = [
        "#0D5559", // teal
        "#8A3C0F", // peach
        "#5B14B8", // mauve
    ]

    switch (format.toLowerCase())
    {
        case "fvb":  return colors[0]
        case "epub": return colors[1]
        default:     return colors[3]
    }
}