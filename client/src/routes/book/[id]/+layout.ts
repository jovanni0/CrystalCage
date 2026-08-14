import { BookStore }          from "@stores/BookStore.svelte"
import { api_client, logger } from "@root/CompositionRoot"



export const load = async ({ params, parent }) => 
{
    await parent()

    const id = params.id
    const book_store = new BookStore(id, logger, api_client)

    return { book_store }
}