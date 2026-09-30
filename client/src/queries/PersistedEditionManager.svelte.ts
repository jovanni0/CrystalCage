import type { EditionResponse } from "@root/dtos/EditionResponse";
import type { EditionManager } from "./EditionManager.interface";
import type { DisplayEdition } from "@root/types/DisplayEdition";
import type { ApiClient } from "@root/utils/ApiClient";
import type { EditionCreateRequest } from "@root/dtos/EditionCreateRequest";



export class PersistedEditionManager implements EditionManager
{
    private server_editions: EditionResponse[] = $state([])
    
    public editions: DisplayEdition[] = $derived(this.server_editions.map(it => this.responseToEdition(it)))


    constructor(
        private book_id: string,
        private api: ApiClient,
    )
    {}


    async initialize(): Promise<void>
    {
        this.server_editions = await this.api.getEditions(this.book_id)
    }


    public async addEdition(file: File): Promise<void>
    {
        const request: EditionCreateRequest = {
            book_id: this.book_id,
            label: null,
            release_date: null,
            format: file.name.split('.').at(-1) ?? "unknown",
            file: file,
        }

        const edition = await this.api.createEdition(request)

        this.server_editions.push(edition)
    }


    public async removeEdition(id: string): Promise<void>
    {
        await this.api.deleteEdition(id)

        this.server_editions = this.server_editions.filter(it => it.id !== id)
    }


    public async setEditionLabel(id: string, label: string): Promise<void>
    {
        const updated = await this.api.updateEdition(id, { label })

        this.server_editions = this.server_editions.map(it => it.id === updated.id ? updated : it)
    }


    private responseToEdition(response: EditionResponse): DisplayEdition
    {
        return {
            id: response.id,
            label: response.label,
            format: response.format,
            added_at: response.added_at,
        }
    }
}