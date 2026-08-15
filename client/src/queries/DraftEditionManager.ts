import type { EditionDraft } from "@root/types/EditionDraft";
import type { EditionManager } from "./EditionManager.interface";
import { EditionQuery } from "./EditionQuery";
import type { DisplayEdition } from "@root/types/DisplayEdition";


export class DraftEditionManager implements EditionManager
{
    private edition_query: EditionQuery = new EditionQuery()
    private drafts: EditionDraft[] = $state([])

    public editions: DisplayEdition[] = $derived(this.drafts.map(it => this.draftToEdition(it)))


    public addEdition(file: File): void
    {
        const edition = this.edition_query.prepareEdition(file)
        this.drafts.push(edition)
    }


    public removeEdition(id: string): void
    {
        this.drafts = this.edition_query.removeEdition(this.drafts, id)
    }


    public setEditionLabel(id: string, label: string): void
    {
        this.edition_query.setTitle(this.drafts, id, label)
    }


    public getDrafts(): EditionDraft[]
    {
        return this.drafts
    }


    private draftToEdition(draft: EditionDraft): DisplayEdition
    {
        return {
            id: draft.local_id,
            label: draft.title,
            format: draft.format,
            added_at: null
        }
    }
}