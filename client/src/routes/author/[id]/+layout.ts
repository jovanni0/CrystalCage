import { api_client, logger } from '@root/CompositionRoot.js'
import { AuthorStore }        from '@root/stores/AuthorStore.svelte.js'



export const load = async ({ params, parent }) => 
{
    await parent()

    const id = params.id
    const author_store = new AuthorStore(id, logger, api_client)

    return { author_store }
}