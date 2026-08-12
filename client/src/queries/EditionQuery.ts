import type { EditionDraft } from "@root/types/EditionDraft"



export class EditionQuery
{
    public prepareEdition(file: File): EditionDraft
    {
        const local_id = crypto.randomUUID()
        const format = file.name.split('.').at(-1) ?? "unknown"

        return { local_id, file, format, title: null, created_at: new Date() }
    }


    public removeEdition(editions: EditionDraft[], local_id: string): EditionDraft[]
    {
        return editions.filter(it => it.local_id !== local_id)
    }


    public setTitle(editions: EditionDraft[], local_id: string, title: string): void
    {
        const edition = editions.find(it => it.local_id === local_id)

        if (edition)
        {
            edition.title = title
        }
    }

    public findByLocalId(editions: EditionDraft[], local_id: string | null): EditionDraft | null
    {
        return editions.find(it => it.local_id === local_id) ?? null
    }
}