import { ApiClient }                  from "./utils/ApiClient.ts";
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
import { CoverRepository }            from "@v2/lib/repositories/cover-repository.svelte";
import { Session }                    from "@v2/lib/states/session.svelte";
import { SessionStorage }             from "@v2/lib/storage/session-storage";
import { ApiAuth }                    from "@v2/lib/clients/api-auth";
import { HttpClient }                 from "@v2/lib/clients/http-client";
import { SessionHttp }                from "@v2/lib/clients/session-http";
import { Nav }                        from "@v2/lib/utils/nav";



export const session             = new Session(new SessionStorage())
export const logger              = new ConsoleLogger("DEBUG")
export const api_client          = new ApiClient(logger, session)
export const mock_api            = new MockApi()
export const library_store       = new LibraryStore(logger, api_client)
export const visibility_observer = new VisibilityObserver()

export const edition_format_color_registry = new EditionFormatColorRegistry()

export const nav = new Nav()


// APIs
export const http_client = new HttpClient(logger)
export const session_http = new SessionHttp(http_client, session)
export const api_auth = new ApiAuth(http_client, session_http)


// repositories
const repo_api = api_client
export const author_repository = new AuthorRepository(repo_api, logger)
export const universe_repository = new UniverseRepository(repo_api, logger)
export const series_repository = new SeriesRepository(repo_api, logger)
export const tag_repository = new TagRepository(repo_api, logger)
export const edition_repository = new EditionRepository(repo_api, logger)
export const book_repository = new BookRepository(repo_api, logger)
export const cover_repository = new CoverRepository(repo_api, logger)

const repos = [author_repository, universe_repository, series_repository, tag_repository, edition_repository, book_repository, cover_repository]
session.onSignOut( () => repos.forEach(it => it.reset()) )