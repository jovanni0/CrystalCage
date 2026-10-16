import { parse } from "yaml"



export type FrontmatterResult =
{
    data: Record<string, unknown>
    body: string
}



const FRONTMATTER_REGEX = /^---\r?\n([\s\S]*?)\r?\n---\r?\n?/



export function splitFrontmatter(source: string): FrontmatterResult
{
    const match = source.match(FRONTMATTER_REGEX)

    if (!match)
    {
        return { data: {}, body: source }
    }

    return {
        data: parse(match[1]) ?? {},
        body: source.slice(match[0].length),
    }
}