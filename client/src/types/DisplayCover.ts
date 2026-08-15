


export type DisplayCover = {
    id:         string
    label:      string | null
    is_default: boolean
    image_source: { kind: "file", file: File } | { kind: "cover_id", cover_id: string }
}