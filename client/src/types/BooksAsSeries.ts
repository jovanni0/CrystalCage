import type { LibraryItem } from "./LibraryItem"



export type BooksBySeries = {
    id:            string
    title:         string
    universe_name: string
    items:         LibraryItem[]
}