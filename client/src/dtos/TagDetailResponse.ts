import type { TagBookItem } from "./TagBookItem"



export type TagDetailResponse = {
    id:          string
    category_id: string | null
    name:        string
    description: string | null
    books:       TagBookItem[]
}