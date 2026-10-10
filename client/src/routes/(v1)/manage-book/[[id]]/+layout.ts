import { api_client, logger }  from "@root/CompositionRoot"
import { BookManagementStore } from "@root/stores/BookManagementStore.svelte"



export const load = async ({ params, parent }) =>
{
    await parent()

    const id = params.id
    const book_store = new BookManagementStore(logger, api_client)

    await book_store.initialize(id ?? null)

    return { book_store, id }
}