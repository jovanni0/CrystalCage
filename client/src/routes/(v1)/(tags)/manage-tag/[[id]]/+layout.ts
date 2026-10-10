import { api_client, logger } from "@root/CompositionRoot";
import { TagManagementStore } from "@stores/TagMangementStore.svelte";



export const load = async ({ params, parent }) => 
{
    await parent()

    const id = params.id
    const tag_store = new TagManagementStore(api_client, logger, id ?? null)

    await tag_store.initialize()

    return { tag_store, id }
}
