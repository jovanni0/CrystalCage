import type { SeriesBookItem } from "./SeriesBookItem"



export type SeriesDetailResponse = {
    id:          string
    author_id:   string
    universe_id: string | null
    name:        string
    is_finished: boolean
    books:       SeriesBookItem[]
}