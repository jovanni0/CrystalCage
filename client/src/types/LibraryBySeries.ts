import type { LibraryItem } from "./LibraryItem"



export type LibraryBySeries = {
    author_name:   string
    universe_name: string
    items:         LibraryItem[]
}