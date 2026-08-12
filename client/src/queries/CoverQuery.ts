import type { CoverDraft } from "@root/types/CoverDraft"



export class CoverQuery
{
    public prepareCover(file: File): CoverDraft
    {
        const local_id = crypto.randomUUID()
        const preview_url = URL.createObjectURL(file)

        return { local_id, file, preview_url, title: null }
    }


    public removeCover(covers: CoverDraft[], local_id: string): CoverDraft[]
    {
        const cover = covers.find(it => it.local_id === local_id)

        if (cover)
        {
            URL.revokeObjectURL(cover.preview_url)
        }

        return covers.filter(it => it.local_id !== local_id)
    }


    public setTitle(covers: CoverDraft[], local_id: string, title: string): void
    {
        const cover = covers.find(it => it.local_id === local_id)

        if (cover)
        {
            cover.title = title
        }
    }


    public findByLocalId(covers: CoverDraft[], local_id: string | null): CoverDraft | null
    {
        if (local_id === null)
        {
            return null
        }
        
        return covers.find(it => it.local_id === local_id) ?? null
    }
}