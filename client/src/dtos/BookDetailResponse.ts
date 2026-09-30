


export type BookDetailResponse = {
    id:            string
    author_id:     string
    series_id:     string | null
    universe_id:   string | null
    cover_id:      string | null
    volume_number: number | null
    title:         string
    launch_date:   string | null
    word_count:    number | null
    blurb:         string | null
    notes:         string | null
    tag_ids:       string[]
    read_status:   "UNREAD" | "IN_PROGRESS" | "DNF" | "READ"
    reread_count:  number
}