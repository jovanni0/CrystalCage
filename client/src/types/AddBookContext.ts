


export type AddBookContext = {
    author_id:        string | null
    universe_id:      string | null
    series_id:        string | null
    volume_number:    number | null
    title:            string | null
    launch_date:      string | null
    word_count:       number | null
    blurb:            string | null
    notes:            string | null
    tag_ids:          string[]
    default_cover_id: string | null
}