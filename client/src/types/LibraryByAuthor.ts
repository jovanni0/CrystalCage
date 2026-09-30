import type { LibraryItem } from "./LibraryItem"



export type LibraryByAuthor = {
    author_id:   string
    author_name: string
    items:       LibraryItem[]
}