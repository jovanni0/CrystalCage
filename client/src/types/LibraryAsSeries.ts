import type { LibraryItem } from "./LibraryItem"



export type LibraryAsSeries = {
    author_id:     string
    author_name:   string
    universe_name: string
    items:         LibraryItem[]
}