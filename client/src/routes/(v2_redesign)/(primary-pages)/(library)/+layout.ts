import { author_repository, book_repository, edition_repository, read_progress_repository, read_record_repository } from "@root/CompositionRoot"
import { error } from "@sveltejs/kit"
import { LibStore } from "@v2/lib/stores/library-store.svelte"
import { describeError } from "@v2/lib/utils/result"



export const load = async ({ parent }) =>
{
    await parent()

    const result = await LibStore.create({
        books:         book_repository,
        authors:       author_repository,
        editions:      edition_repository,
        read_records:  read_record_repository,
        read_progress: read_progress_repository,
    })

    if (!result.ok)
    {
        error(500, describeError(result.error))
    }

    return { library_store: result.value }
}