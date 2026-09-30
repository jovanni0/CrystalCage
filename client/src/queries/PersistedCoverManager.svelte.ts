import type { CoverResponse } from "@root/dtos/CoverResponse";
import type { CoverManager } from "./CoverManager.interface";
import type { DisplayCover } from "@root/types/DisplayCover";
import type { ApiClient } from "@root/utils/ApiClient";
import type { CoverCreateRequest } from "@root/dtos/CoverCreateRequest";



export class PersistedCoverManager implements CoverManager
{
    private server_covers: CoverResponse[] = $state([])
 
    public default_id: string | null  = $derived(this.server_covers.find(c => c.is_default)?.id ?? null)
    public covers:     DisplayCover[] = $derived(this.server_covers.map(it => this.responseToCover(it)))


    constructor(
        private book_id: string,
        private api: ApiClient,
    )
    {}


    async initialize(): Promise<void>
    {
        this.server_covers = await this.api.getCovers(this.book_id)
    }


    public async addCover(file: File): Promise<void>
    {
        const request: CoverCreateRequest = {
            book_id: this.book_id,
            label: null,
            file: file
        }

        const cover = await this.api.createCover(request)

        if (this.default_id === null)
        {
            this.setDefaultCoverId(cover.id)
        }
    }


    public async removeCover(id: string): Promise<void>
    {
        await this.api.deleteCover(id)

        this.server_covers = await this.api.getCovers(this.book_id)
    }


    public async setDefaultCoverId(id: string): Promise<void>
    {
        await this.api.updateBook(this.book_id, { cover_id: id})

        this.server_covers = this.server_covers.map(it => ({ ...it, is_default: it.id === id }))
    }


    public async setCoverLabel(id: string, label: string): Promise<void>
    {
        const updated = await this.api.updateCover(id, { label })

        this.server_covers = this.server_covers.map(it => it.id === updated.id ? updated : it)
    }


    private responseToCover(response: CoverResponse): DisplayCover
    {
        return {
            id: response.id,
            label: response.label,
            is_default: response.is_default,
            image_source: { 
                kind: "REMOTE", 
                id: response.id 
            }
        }
    }
}