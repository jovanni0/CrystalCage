import type { EditionData } from "../data-models/edition-data"
import type { ReadTarget }  from "../data-models/read-target"



export type ReadDraft = {
    target:        ReadTarget
    start_date:    string
    end_date:      string
    edition_id:    string | null
    notes:         string
    is_historical: boolean
    is_abandoned:  boolean
}