


export type ReadRecordUpdateRequest = {
    edition_id?:     string | null
    start_date?:     string | null
    end_date?:       string | null
    did_not_finish?: boolean
    is_historical?:  boolean
    stop_position?:  string | null
    notes?:          string | null
}