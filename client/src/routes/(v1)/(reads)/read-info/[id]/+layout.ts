import { api_client, logger } from "@root/CompositionRoot";
import { ReadManagementStore } from "@stores/ReadManagementStore.svelte";



export const load = async ({ params, parent }) => 
{
    await parent()

    const id = params.id
    const read_store = new ReadManagementStore(api_client, logger, id)

    await read_store.initialize()

    return { read_store, id }
}
