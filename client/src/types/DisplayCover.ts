import type { CoverSource } from "./CoverSource"



export type DisplayCover = {
    id:           string
    label:        string | null
    is_default:   boolean
    image_source: CoverSource
}