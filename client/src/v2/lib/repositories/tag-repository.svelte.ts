import type { Logger }              from "@root/loggers/Logger"
import type { AsyncResult }         from "../utils/result"
import type { APIClient }           from "../clients/api-client.interface"
import type { TagCategoryResponse } from "../dtos/tag-category.dto"
import type { TagCreateRequest, 
              TagResponse }         from "../dtos/tag.dto"
import      { attempt }             from "../utils/result"
import      { ListCache }           from "./_list-cache.svelte"



class TagCategoryCache extends ListCache<TagCategoryResponse>
{
    constructor(private api: APIClient, logger: Logger) 
    { 
        super(logger, "tag categories") 
    }


    protected fetchAll(): Promise<TagCategoryResponse[]>
    { 
        // return this.api.getTagCategories() 
        return Promise.resolve([])
    }
}



export class TagRepository extends ListCache<TagResponse>
{
    public readonly categories: TagCategoryCache


    constructor(private api: APIClient, logger: Logger)
    {
        super(logger, "tags")

        this.categories = new TagCategoryCache(api, logger)
    }


    protected fetchAll(): Promise<TagResponse[]> 
    { 
        return this.api.getTags() 
    }

    
    public override async load(max_age_ms = 60_000): AsyncResult<TagResponse[]>
    {
        const [tags, categories] = await Promise.all([
            super.load(max_age_ms), 
            this.categories.load(max_age_ms)
        ])

        return categories.ok ? tags : categories
    }


    public override reset()
    {
        super.reset()
        this.categories.reset()
    }


    public async create(request: TagCreateRequest): AsyncResult<TagResponse>
    {
        return attempt(this.logger, "create tag", async () =>
        {
            const tag = await this.api.createTag(request)
            const amended_tag = { ...tag, category_id: null }

            this.add(amended_tag)
            
            return amended_tag
        })
    }
}