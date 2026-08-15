


export type BookUpdateRequest = {
    author_id?:     string
    universe_id?:   string | null
    series_id?:     string | null
    cover_id?:      string | null
    volume_number?: number | null
    title?:         string
    launch_date?:   string | null
    word_count?:    number | null
    blurb?:         string | null
    notes?:         string | null
    tag_ids?:       string[]
}