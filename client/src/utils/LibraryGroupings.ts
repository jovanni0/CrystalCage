import type { AuthorResponse }      from "@root/dtos/AuthorResponse"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import type { SeriesResponse }      from "@root/dtos/SeriesResponse"
import type { UniverseResponse }    from "@root/dtos/UniverseResponse"
import type { LibraryByAuthor }     from "@root/types/LibraryByAuthor"
import type { LibraryBySeries }     from "@root/types/LibraryBySeries"
import type { LibraryItem }         from "@root/types/LibraryItem"



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


export function groupAsSeries(authors: AuthorResponse[], universes: UniverseResponse[], series: SeriesResponse[], books: BookSummaryResponse[]): LibraryBySeries[]
{
    const author_id_name = new Map(authors.map(it => [it.id, formatAuthorName(it)]))
    const universe_id_name = new Map(universes.map(it => [it.id, it.name]))
    const series_id_name = new Map(series.map(it => [it.id, it.name]))

    const grouped = new Map<string, Map<string, Map<string, LibraryItem>>>()

    for (const book of books) 
    {
        const author_id = book.author_id
        const universe_id = book.universe_id ?? "none"
        const series_id = book.series_id ?? "standalone"

        const by_universe = grouped.get(author_id) ?? new Map()
        grouped.set(author_id, by_universe)

        const by_series = by_universe.get(universe_id) ?? new Map()
        by_universe.set(universe_id, by_series)

        let item = by_series.get(series_id)

        if (!item) 
        {
            item = {
                id: book.id,
                name: series_id_name.get(book.series_id ?? "") ?? "standalone",
                cover_id: book.cover_id,
                number_of_volumes: 0,
            }

            by_series.set(series_id, item)
        }

        item.number_of_volumes++
    }

    return [...grouped.entries()].flatMap( ([author_id, by_universe]) =>
        [...by_universe.entries()].map( ([universe_id, by_series]) => ({
            author_name: author_id_name.get(author_id) ?? "Unknown",
            universe_name: universe_id === "none" ? "Standalone" : universe_id_name.get(universe_id) ?? "Unknown",
            items: [...by_series.values()],
        }))
    )
}