import { edition_repository, logger } from "@root/CompositionRoot"
import { EditionManagerStore }          from "@v2/lib/stores/edition-manager.store.svelte"
import { error }                        from "@sveltejs/kit"



export const load = async ({ params, parent }) =>
{
    await parent()

    const result = await EditionManagerStore.create(params.id, { editions: edition_repository }, logger)

    if (!result.ok)
    {
        error(503, result.error.message)
    }

    return { store: result.value }
}
