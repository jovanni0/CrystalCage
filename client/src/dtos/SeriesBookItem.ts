import type { ReadStatus } from "@root/types/ReadStatus"



export type SeriesBookItem = {
    id:            string
    cover_id:      string | null
    volume_number: number | null
    title:         string
    launch_date:   string | null
    word_count:    number | null
    read_status:   ReadStatus
    reread_count:  number
}