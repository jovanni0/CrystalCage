


export type CoverCreateRequest = {
    book_id: string
    label:   string | null
    file:    File
}


export function coverCreateRequestToFormData(request: CoverCreateRequest): FormData 
{
    const form_data = new FormData()

    form_data.append('book_id', request.book_id)
    if (request.label) form_data.append('label', request.label)
    form_data.append('file', request.file)

    return form_data
}