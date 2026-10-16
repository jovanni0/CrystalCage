import type { RenderedMarkdown } from "./markdown"
import type { Glossary }         from "./directives"
import      { parse }            from "yaml"
import      { splitFrontmatter } from "./frontmatter"
import      { renderMarkdown }   from "./markdown"



export type DocMeta =
{
    title: string
    description: string
    nav_label: string
    order: number
    status?: string
}

export type Doc = RenderedMarkdown &
{
    slug: string
    meta: DocMeta
}



const DOC_FILES = import.meta.glob("/content/docs/*.md", { query: "?raw", import: "default", eager: true }) as Record<string, string>
const DATA_FILES = import.meta.glob("/content/*.yaml", { query: "?raw", import: "default", eager: true }) as Record<string, string>



export function readDataFile<TypeT>(file_name: string): TypeT
{
    const source = DATA_FILES[`/content/${ file_name }.yaml`]

    if (source === undefined)
    {
        throw new Error(`Missing content file content/${ file_name }.yaml`)
    }

    return parse(source) as TypeT
}



export function docSlugs(): string[]
{
    return Object.keys(DOC_FILES).map((path) => path.replace("/content/docs/", "").replace(".md", ""))
}



export async function loadDoc(slug: string): Promise<Doc | null>
{
    const source = DOC_FILES[`/content/docs/${ slug }.md`]

    if (source === undefined)
    {
        return null
    }

    const { data, body } = splitFrontmatter(source)
    const rendered = await renderMarkdown(body, readDataFile<Glossary>("glossary"))

    return { slug, meta: data as DocMeta, ...rendered }
}