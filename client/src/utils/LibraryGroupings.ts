import type { AuthorResponse }      from "@root/dtos/AuthorResponse"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import type { SeriesResponse }      from "@root/dtos/SeriesResponse"
import type { UniverseResponse }    from "@root/dtos/UniverseResponse"
import type { BooksBySeries } from "@root/types/BooksAsSeries"
import type { LibraryByAuthor }     from "@root/types/LibraryByAuthor"
import type { LibraryAsSeries }     from "@root/types/LibraryAsSeries"
import type { LibraryItem }         from "@root/types/LibraryItem"
import type { ReadRecordResponse } from "@root/dtos/ReadRecordResponse"
import type { EditionResponse } from "@root/dtos/EditionResponse"
import type { ReadHistoryItem } from "@root/types/ReadHistoryItem"
import type { TagBookItem } from "@root/dtos/TagBookItem"
import type { TagBookLink } from "@root/stores/TagMangementStore.svelte"



export function formatAuthorName(author: AuthorResponse): string
{
    return `${author.last_name}, ${author.first_name}`
}


export function groupByAuthor(authors: AuthorResponse[], books: BookSummaryResponse[]): LibraryByAuthor[]
{
    const author_name_id = new Map(authors.map(it => [it.id, formatAuthorName(it)]))
    const groupes = new Map<string, LibraryByAuthor>()

    for (const book of books)
    {
        let author_group = groupes.get(book.author_id)

        if (!author_group)
        {
            author_group = {
                author_id: book.author_id,
                author_name: author_name_id.get(book.author_id) ?? "Unknown",
                items: []
            }

            groupes.set(book.author_id, author_group)
        }

        author_group.items.push({
            id: book.id,
            name: book.title,
            cover_id: book.cover_id,
            number_of_volumes: 0
        })
    }

    return [...groupes.values()]
}


export function groupAsSeries(authors: AuthorResponse[], universes: UniverseResponse[], series: SeriesResponse[], books: BookSummaryResponse[]): LibraryAsSeries[] 
{
    const author_id_name:   Map<string, string> = new Map(authors.map(it => [it.id, formatAuthorName(it)]))
    const universe_id_name: Map<string, string> = new Map(universes.map(it => [it.id, it.name]))
    const series_id_name:   Map<string, string> = new Map(series.map(it => [it.id, it.name]))

    const groups = new Map<string, { group: LibraryAsSeries; items_map: Map<string, LibraryItem> }>()

    for (const book of books) 
    {
        const group_key = `${book.author_id}:${book.universe_id ?? "none"}`

        if (!groups.has(group_key)) 
        {
            const new_group = {
                author_id: book.author_id,
                author_name: author_id_name.get(book.author_id) ?? 'Unknown',
                universe_name: book.universe_id ? universe_id_name.get(book.universe_id) ?? 'Unknown' : 'Standalone',
                items: [],
            }

            groups.set(group_key, { group: new_group, items_map: new Map() })
        }

        const { group, items_map: items_map } = groups.get(group_key)!
        const series_key = book.series_id ?? "standalone"

        if (!items_map.has(series_key)) 
        {
            const item: LibraryItem = {
                id: book.id,
                name: book.series_id ? series_id_name.get(book.series_id) ?? "Standalone" : "Standalone",
                cover_id: book.cover_id,
                number_of_volumes: 0,
            }

            items_map.set(series_key, item)
            group.items.push(item)
        }

        items_map.get(series_key)!.number_of_volumes++
    }

    return Array.from(groups.values(), it => it.group)
}


export function groupBySeries(books: BookSummaryResponse[], universes: UniverseResponse[], series: SeriesResponse[])
{
    const standalone_key:   string                     = "standalone"
    const universe_id_name: Map<string, string>        = new Map(universes.map(it => [it.id, it.name]))
    const series_id_name:   Map<string, string>        = new Map(series.map(it => [it.id, it.name]))
    const grouped:          Map<string, BooksBySeries> = new Map()

    for (const book of books) 
    {
        const item: LibraryItem = {
            id: book.id,
            name: book.title,
            cover_id: book.cover_id,
            number_of_volumes: 0
        }

        if (!book.series_id)
        {
            if (!grouped.has(standalone_key))
            {
                const group: BooksBySeries = { 
                    id: crypto.randomUUID(),
                    title: "Standalone", 
                    universe_name: "", 
                    items: []
                }

                grouped.set(standalone_key, group)
            }

            grouped.get(standalone_key)!.items.push(item)
            continue
        }

        const key = `${book.universe_id ?? 'no_univ'}:${book.series_id}`

        if (!grouped.has(key))
        {
            const group: BooksBySeries = {
                id: crypto.randomUUID(),
                title: series_id_name.get(book.series_id) ?? "(Unknown Series)",
                universe_name: book.universe_id ? universe_id_name.get(book.universe_id) ?? "" : "",
                items: []
            }

            grouped.set(key, group)
        }

        grouped.get(key)!.items.push(item)
    }

    return Array.from(grouped.values())
}


export function buildReadHistory(records: ReadRecordResponse[], editions: EditionResponse[]): ReadHistoryItem[]
{
    const edition_map: Map<string, EditionResponse> = new Map(editions.map(it => [it.id, it]))

    return records.map(it =>
    {
        const edition = it.edition_id ? edition_map.get(it.edition_id) : undefined

        return {
            id: it.id,
            start_date: it.start_date,
            end_date: it.end_date,
            did_not_finish: it.did_not_finish,
            is_historical: it.is_historical,
            notes: it.notes,
            edition_name: edition?.label ?? "(Unknown Edition)",
            edition_release_date: edition?.release_date ?? "",
            edition_format: edition?.format ?? "(Unknown)",
        }
    })
}


export function buildTagUsage(books: TagBookItem[], series: SeriesResponse[]): TagBookLink[]
{
    const series_map: Map<string, string> = new Map(series.map(it => [it.id, it.name]))

    return books.map(it =>
    {
        const series = it.series_id ? series_map.get(it.series_id) ?? it.series_id : null

        return {
            book_id: it.id,
            book_title: it.title,
            series_name: series,
            cover_id: it.cover_id
        }
    })
}