import type { CoverDraft } from "@root/types/CoverDraft"
import type { EditionDraft } from "@root/types/EditionDraft"
import type { AddBookContext } from "../types/AddBookContext"



/**
 * `launch_date` and `release_date` should have the format `YYYY-MM-DD`.
 */
export type BookCreateRequest = {
    author_id:      string
    universe_id?:   string
    series_id?:     string
    volume_number?: number
    title:          string
    launch_date?:   string
    word_count?:    number
    blurb?:         string
    notes?:         string
    tag_ids?:       string[]
    covers?: {
        file:       File
        label?:     string
        is_default: boolean
    }[]
    editions?: {
        file:          File
        label?:        string
        format:        string
        release_date?: string
    }[]
}



export function toFormData(request: BookCreateRequest): FormData
{
    const form = new FormData()

    form.append("author_id", request.author_id)
    if (request.universe_id) form.append("universe_id", request.universe_id)
    if (request.series_id) form.append("series_id", request.series_id)
    if (request.volume_number !== undefined) form.append("volume_number", String(request.volume_number))
    form.append("title", request.title)
    if (request.launch_date) form.append("launch_date", request.launch_date)
    if (request.word_count !== undefined) form.append("word_count", String(request.word_count))
    if (request.blurb) form.append("blurb", request.blurb)
    if (request.notes) form.append("notes", request.notes)

    request.tag_ids?.forEach(id => form.append("tag_ids", id))

    request.covers?.forEach( (cover, i) => 
    {
        if (cover.label) 
            form.append(`covers[${i}].label`, cover.label)

        form.append(`covers[${i}].is_default`, String(cover.is_default))
        form.append(`covers[${i}].file`, cover.file, cover.file.name)
    })

    request.editions?.forEach( (edition, i) => 
    {
        if (edition.label) 
            form.append(`editions[${i}].label`, edition.label)
        if (edition.release_date) 
            form.append(`editions[${i}].release_date`, edition.release_date)

        form.append(`editions[${i}].format`, edition.format)
        form.append(`editions[${i}].file`, edition.file, edition.file.name)
    })

    return form
}



export function toBookCreateRequest2(context: AddBookContext, covers: CoverDraft[], editions: EditionDraft[]): BookCreateRequest
{
    return {
        author_id: context.author_id!,
        universe_id: context.universe_id ?? undefined,
        series_id: context.series_id ?? undefined,
        volume_number: context.volume_number ?? undefined,
        title: context.title!,
        launch_date: context.launch_date?.toISOString().slice(0, 10),
        word_count: context.word_count ?? undefined,
        blurb: context.blurb ?? undefined,
        notes: context.notes ?? undefined,
        tag_ids: context.tag_ids,
        covers: covers.map(c => ({
            file: c.file,
            label: c.title ?? undefined,
            is_default: c.local_id === context.default_cover_id
        })),
        editions: editions.map(e => ({
            file: e.file,
            label: e.title ?? undefined,
            format: e.format,
            release_date: e.created_at?.toISOString().slice(0, 10)
        }))
    }
}