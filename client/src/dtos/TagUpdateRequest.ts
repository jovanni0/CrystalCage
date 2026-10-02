


export type TagUpdateRequest = {
    name?:             string
    category_id?:      string | null
    description?:      string | null
    added_book_ids?:   string[]
    removed_book_ids?: string[]
}