import { ApiClient }                  from "./utils/ApiClient.ts";
import { SessionStorage }             from "./utils/SessionStorage.ts";
import { SessionManager }             from "./utils/SessionManager.svelte.ts";
import { ConsoleLogger }              from "@loggers/ConsoleLogger";
import { LibraryStore }               from "./stores/LibraryStore.svelte.ts";
import { VisibilityObserver }         from "./utils/VisibilityObserver.ts";
import { MockApi }                    from "./v2/lib/clients/mock-api.ts";
import { EditionFormatColorRegistry } from "@v2/lib/utils/format-color-registry.svelte";
import { AuthorRepository }           from "@v2/lib/repositories/author-repository.svelte";
import { UniverseRepository }         from "@v2/lib/repositories/universe-repository.svelte";
import { SeriesRepository }           from "@v2/lib/repositories/series-repository.svelte";
import { TagRepository }              from "@v2/lib/repositories/tag-repository.svelte";
import { EditionRepository }          from "@v2/lib/repositories/edition-repository.svelte";
import { BookRepository }             from "@v2/lib/repositories/book-repository.svelte";



export const logger              = new ConsoleLogger("DEBUG")
export const api_client          = new ApiClient(logger)
export const mock_api            = new MockApi()
export const session_storage     = new SessionStorage()
export const session_manager     = new SessionManager(api_client, session_storage, logger)
export const library_store       = new LibraryStore(logger, api_client)
export const visibility_observer = new VisibilityObserver()
export const edition_format_color_registry = new EditionFormatColorRegistry()


// repositories
const repo_api = api_client
export const author_repository = new AuthorRepository(repo_api, logger)
export const universe_repository = new UniverseRepository(repo_api, logger)
export const series_repository = new SeriesRepository(repo_api, logger)
export const tag_repository = new TagRepository(repo_api, logger)
export const edition_repository = new EditionRepository(repo_api, logger)
export const book_repository = new BookRepository(repo_api, logger)