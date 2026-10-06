import { logger } from "@root/CompositionRoot"
import { CreateBookStore } from "@v2/lib/stores/book-management.store.svelte"



export const load = async ({ params, parent }) =>
{
    await parent()

    const id = params.id ?? null

    const book_store = new CreateBookStore(logger)
    await book_store.initialize(id)

    return { book_store, id  }
}