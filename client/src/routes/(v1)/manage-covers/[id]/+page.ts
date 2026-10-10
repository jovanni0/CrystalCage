import { book_repository, cover_repository, logger } from "@root/CompositionRoot"
import { CoverManagerStore }                         from "@v2/lib/stores/cover-manager.store.svelte"
import { error }                                     from "@sveltejs/kit"



export const load = async ({ params, parent }) =>
{
    await parent()

    const dependencies = { covers: cover_repository, books: book_repository }
    const result       = await CoverManagerStore.create(params.id, dependencies, logger)

    if (!result.ok)
    {
        error(503, result.error.message)
    }

    return { store: result.value }
}
