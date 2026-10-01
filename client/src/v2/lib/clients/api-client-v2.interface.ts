import type { TagCategoryResponse } from "../dtos/tag-category.dto";



export interface ApiClientV2
{
    getTagCategories(): Promise<TagCategoryResponse[]>
}