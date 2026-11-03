import type { SearchEntry } from "#lib/content/search-index.ts"



export type SnippetPart =
{
    text:     string
    is_match: boolean
}

export type SearchResult =
{
    entry:   SearchEntry
    score:   number
    snippet: SnippetPart[]
}



const MAX_RESULTS     = 8
const SNIPPET_CONTEXT = 60



// lowercase, without accents, so "configuratie" also finds "configurație"
function normalize(text: string): string
{
    return text.normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase()
}


function wordsOf(text: string): string[]
{
    return normalize(text).split(/[^\p{L}\p{N}]+/u).filter((word) => word !== "")
}



// every query word must appear (as the start of a word); matches in titles count more than in the text
export function search(entries: SearchEntry[], query: string): SearchResult[]
{
    const query_words = wordsOf(query)

    if (query_words.length === 0)
    {
        return []
    }

    const results: SearchResult[] = []

    for (const entry of entries)
    {
        const title_words = wordsOf(`${ entry.page } ${ entry.section ?? "" }`)
        const text_words = wordsOf(entry.text)

        let score = 0
        let matches_all = true

        for (const query_word of query_words)
        {
            let title_score = 0

            if (title_words.some((word) => word === query_word))
            {
                title_score = 15
            }
            else if (title_words.some((word) => word.startsWith(query_word)))
            {
                title_score = 10
            }

            const text_hits = text_words.filter((word) => word.startsWith(query_word)).length

            if (title_score === 0 && text_hits === 0)
            {
                matches_all = false
                break
            }

            score += title_score + Math.min(text_hits, 3) * 2
        }

        if (matches_all)
        {
            results.push({ entry, score, snippet: buildSnippet(entry.text, query_words) })
        }
    }

    return results
        .sort((first, second) => second.score - first.score)
        .slice(0, MAX_RESULTS)
}



// a short piece of text around the first match, split into parts so the matches can be highlighted
function buildSnippet(text: string, query_words: string[]): SnippetPart[]
{
    const normalized = normalize(text)
    const words_pattern = query_words.map(escapeRegex).join("|")

    const first_match = normalized.search(new RegExp(`(?<![\\p{L}\\p{N}])(${ words_pattern })`, "u"))
    const anchor = Math.max(first_match, 0)

    let start = Math.max(0, anchor - SNIPPET_CONTEXT)
    const end = Math.min(text.length, anchor + SNIPPET_CONTEXT * 2)

    // start at a whole word, not in the middle of one
    if (start > 0)
    {
        const next_space = text.indexOf(" ", start)

        if (next_space !== -1 && next_space < anchor)
        {
            start = next_space + 1
        }
    }

    const excerpt = text.slice(start, end)
    const normalized_excerpt = normalized.slice(start, end)

    const match_pattern = new RegExp(`(?<![\\p{L}\\p{N}])(${ words_pattern })[\\p{L}\\p{N}]*`, "gu")
    const parts: SnippetPart[] = []
    let last_index = 0

    for (const match of normalized_excerpt.matchAll(match_pattern))
    {
        const match_start = match.index ?? 0

        if (match_start > last_index)
        {
            parts.push({ text: excerpt.slice(last_index, match_start), is_match: false })
        }

        parts.push({ text: excerpt.slice(match_start, match_start + match[0].length), is_match: true })
        last_index = match_start + match[0].length
    }

    if (last_index < excerpt.length)
    {
        parts.push({ text: excerpt.slice(last_index), is_match: false })
    }

    if (parts.length > 0)
    {
        if (start > 0)
        {
            parts.unshift({ text: "…", is_match: false })
        }

        if (end < text.length)
        {
            parts.push({ text: "…", is_match: false })
        }
    }

    return parts
}


function escapeRegex(text: string): string
{
    return text.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")
}
