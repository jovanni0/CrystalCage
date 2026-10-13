import { author_repository, 
         book_repository, 
         cover_repository, 
         edition_repository, 
         read_record_repository, 
         series_repository, 
         tag_repository, 
         universe_repository } from '@root/CompositionRoot'
import { error }               from '@sveltejs/kit'
import { BookProfileStore }    from '@v2/lib/stores/book-profile-store.svelte.js'
import { describeError }       from "@v2/lib/utils/result"



export const load = async ({ parent, params }) =>
{
    await parent()

    const result = await BookProfileStore.create({
        books: book_repository,
        authors: author_repository,
        universes: universe_repository,
        series: series_repository,
        read_records: read_record_repository,
        tags: tag_repository,
        cover_repository: cover_repository,
        edition_repository: edition_repository,
    }, params.id)

    if (!result.ok)
    {
        error(result.error.kind === "not_found" ? 404 : 500, describeError(result.error))
    }

    return { book_profile_store: result.value }
}