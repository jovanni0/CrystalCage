import type { TagBookItem } from "./TagBookItem"



export type TagDetailResponse = {
    id:          string
    name:        string
    description: string | null
    books:       TagBookItem[]
}