import type { TagCreateRequest } from "@root/dtos/TagCreateRequest"
import type { Logger } from "@root/loggers/Logger"
import { attempt, type AsyncResult } from "../utils/result"
import type { TagResponse } from "../dtos/tag.dto"
import type { TagCategoryResponse } from "../dtos/tag-category.dto"
import type { APIClient } from "../clients/api-client.interface"



export class TagRepository
{
    private loaded_at: number = 0

    public all:        TagResponse[]         = $state([])
    public categories: TagCategoryResponse[] = $state([])


    constructor(
        private api: APIClient, 
        private logger: Logger
    ) {}


    public async load(max_age_ms = 60_000)
    {
        if (Date.now() - this.loaded_at < max_age_ms) return

        // this.all = await this.api.getTags()
        // this.categories = await this.api.getTagCategories()
        this.loaded_at = Date.now()
    }


    public invalidate() 
    { 
        this.loaded_at = 0 
    }
    

    public reset()
    { 
        this.all = []
        this.loaded_at = 0 
    }


    public async create(request: TagCreateRequest): AsyncResult<TagResponse>
    {
        return attempt(this.logger, "create tag", async () =>
        {
            const tag = await this.api.createTag(request)
            const amended_tag = { ...tag, category_id: null }

            this.all.push(amended_tag)
            
            return amended_tag
        })
    }
}