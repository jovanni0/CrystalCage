import { mock_api } from "@root/CompositionRoot"
import { logger } from "@root/CompositionRoot"
import { CreateBookStore } from "@v2/lib/stores/book-management.store.svelte"



export const load = async ({ params, parent }) =>
{
    await parent()

    const id = null
    const book_store = new CreateBookStore(logger, mock_api, null)

    await book_store.initialize(id ?? null)

    return { book_store, id }
}