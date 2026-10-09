


export type ReadProgressResponse = {
    id:             string
    read_record_id: string
    recorded_at:    string
    progress:       number
}



export type ReadProgressCreateRequest = {
    read_record_id: string
    progress:       number
}