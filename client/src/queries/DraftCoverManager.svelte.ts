import type { CoverDraft } from "@root/types/CoverDraft";
import type { CoverManager } from "./CoverManager.interface";
import type { DisplayCover } from "@root/types/DisplayCover";
import { CoverQuery } from "./CoverQuery";



export class DraftCoverManager implements CoverManager
{
    private cover_query: CoverQuery = new CoverQuery()
    private drafts: CoverDraft[] = $state([])


    public default_id: string | null = $state(null)
    public covers: DisplayCover[] = $derived(this.drafts.map(it => this.draftToCover(it)))


    public addCover(file: File): void
    {
        const cover = this.cover_query.prepareCover(file)
        this.drafts.push(cover)

        if (!this.default_id)
        {
            this.setDefaultCoverId(cover.local_id)
        }
    }


    public removeCover(local_id: string): void
    {
        this.drafts = this.cover_query.removeCover(this.drafts, local_id)

        if (this.default_id === local_id)
        {
            this.default_id = this.drafts[0]?.local_id ?? null
        }
    }


    public setDefaultCoverId(id: string): void
    {
        this.default_id = id
    }


    public setCoverLabel(id: string, label: string): void
    {
        this.cover_query.setTitle(this.drafts, id, label)
    }


    public getDrafts(): CoverDraft[]
    {
        return this.drafts
    }


    private draftToCover(draft: CoverDraft): DisplayCover
    {
        return {
            id: draft.local_id,
            label: draft.title,
            is_default: draft.local_id === this.default_id,
            image_source: { kind: "file", file: draft.file }
        }
    }
}