


export type EditionCreateRequest = {
    book_id:      string
    label:        string | null
    release_date: string | null
    format:       string
    file:         File
}


export function editionCreateRequestToFormData(request: EditionCreateRequest): FormData {
    const formData = new FormData()

    formData.append('book_id', request.book_id)
    if (request.label !== null) formData.append('label', request.label)
    if (request.release_date !== null) formData.append('release_date', request.release_date)
    formData.append('format', request.format)
    formData.append('file', request.file)

    return formData
}