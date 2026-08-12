import type { ReadStatus } from "../types/ReadStatus"



export type BookSummaryResponse = {
    id:             string
    author_id:      string
    universe_id:    string | null
    series_id:      string | null
    cover_id:       string | null
    volume_number:  number | null
    title:          string
    read_status:    ReadStatus
    reread_count:   number
}