import { author_repository, book_repository } from "@root/CompositionRoot"
import type { CaruselItemInfo } from "../elements/currently-reading-carusel.svelte"
import type { BookSummaryResponse } from "@root/dtos/BookSummaryResponse"
import { AuthorRules } from "../rules/author-rules"



export type LibraryItem = {
    book_id:  string
    cover_id: string | null
    label:    string
}



export class LibStore
{
    public items:             LibraryItem[] = $derived(book_repository.all.map(it => ({ book_id: it.id, cover_id: it.cover_id, label: it.title }) ))

    public currently_reading: CaruselItemInfo[] = $derived(book_repository.all.filter(it => it.read_status === "IN_PROGRESS").map(it => this.toCaruselItem(it) ))
    public are_current_reads: boolean = $derived(this.currently_reading.length > 0)
    

    constructor()
    {}


    public async initialize()
    {
        await Promise.all([
            book_repository.load(),
            author_repository.load(),
        ])
    }


    private toCaruselItem(book: BookSummaryResponse): CaruselItemInfo
    {
        const author = author_repository.byId(book.author_id)
        const author_name = author ? AuthorRules.nameOf(author) : "?"
        
        return {
            book_id:  book.id,
            cover_id: book.cover_id,
            title:    book.title,
            author:   author_name,
            progress: 0,
        }
    }
}