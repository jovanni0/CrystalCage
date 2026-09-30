


export type ReadHistoryItem = {
    id: string
    start_date:           string | null
    end_date:             string | null
    did_not_finish:       boolean
    is_historical:        boolean
    notes:                string | null
    edition_name:         string
    edition_release_date: string
    edition_format:       string
}