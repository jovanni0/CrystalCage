import { book_repository } from "@root/CompositionRoot"



export type LibraryItem = {
    book_id:  string
    cover_id: string | null
    label:    string
}



export class LibStore
{
    public items: LibraryItem[] = $derived(book_repository.all.map(it => ({ book_id: it.id, cover_id: it.cover_id, label: it.title }) ))
    
    
    constructor()
    {}


    public async initialize()
    {
        await book_repository.load()
    }
}