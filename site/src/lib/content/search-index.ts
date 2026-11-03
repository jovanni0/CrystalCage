import type { ContentRoadmap } from "./content-roadmap"
import      { docSlugs,
              loadDoc,
              readDataFile }   from "./content"



// one searchable unit: a docs section, or a whole page
export type SearchEntry =
{
    page:    string
    section: string | null
    href:    string
    text:    string
}



const HEADING_PATTERN = /<h([23])[^>]*\sid="([^"]+)"[^>]*>([\s\S]*?)<\/h\1>/g



export async function buildSearchIndex(): Promise<SearchEntry[]>
{
    const entries: SearchEntry[] = []

    for (const slug of docSlugs())
    {
        const doc = await loadDoc(slug)

        if (doc === null)
        {
            continue
        }

        entries.push(...splitIntoSections(doc.html, doc.meta.nav_label ?? doc.meta.title, `/docs/${ slug }`, doc.meta.description))
    }

    entries.push(roadmapEntry())
    entries.push(contactEntry())

    return entries
}



// cuts a rendered doc at every h2/h3; the text before the first heading belongs to the page itself
function splitIntoSections(html: string, page: string, page_href: string, description: string): SearchEntry[]
{
    const entries: SearchEntry[] = []
    const matches = [...html.matchAll(HEADING_PATTERN)]

    const intro_end = matches[0]?.index ?? html.length

    entries.push({ page, section: null, href: page_href, text: `${ description } ${ htmlToText(html.slice(0, intro_end)) }`.trim() })

    matches.forEach((match, index) =>
    {
        const content_start = (match.index ?? 0) + match[0].length
        const content_end = matches[index + 1]?.index ?? html.length

        entries.push({
            page,
            section: htmlToText(match[3]),
            href:    `${ page_href }#${ match[2] }`,
            text:    htmlToText(html.slice(content_start, content_end)),
        })
    })

    return entries
}


function roadmapEntry(): SearchEntry
{
    const roadmap = readDataFile<ContentRoadmap>("roadmap")

    const text = [
        roadmap.caption,
        ...roadmap.staged.map((milestone) => `${ milestone.title }: ${ milestone.text }`),
        roadmap.ideas.title,
        ...roadmap.ideas.items,
    ].join(" ")

    return { page: roadmap.title, section: null, href: "/roadmap", text }
}


function contactEntry(): SearchEntry
{
    return {
        page:    "Contact",
        section: null,
        href:    "/contact",
        text:    "Get in touch. Questions, feedback or ideas about Sanctum. Report a bug, suggest a feature, get help installing.",
    }
}


function htmlToText(html: string): string
{
    return html
        .replace(/<span[^>]*class="term-popover"[^>]*>[\s\S]*?<\/span>/g, " ")   // glossary definitions are not page text
        .replace(/<[^>]+>/g, " ")
        .replace(/&amp;/g, "&")
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&quot;/g, "\"")
        .replace(/&#39;|&#x27;/g, "'")
        .replace(/\s+/g, " ")
        .replace(/ ([,.;:!?)])(?=\s|$)/g, "$1")   // tags were replaced by spaces, "<b>x</b>," must stay "x,"
        .trim()
}
