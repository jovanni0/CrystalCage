import { api_client, logger } from '@root/CompositionRoot'
import { SeriesStore } from '@root/stores/SeriesStore.svelte.js'



export const load = async ({ params, parent }) => 
{
    await parent()

    const id = params.id
    const series_store = new SeriesStore(id, api_client, logger)

    return { series_store }
}