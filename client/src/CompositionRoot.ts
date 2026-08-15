import { ApiClient }          from "./utils/ApiClient.ts";
import { SessionStorage }     from "./utils/SessionStorage.ts";
import { SessionManager }     from "./utils/SessionManager.svelte.ts";
import { ManageBookStore }    from "@stores/ManageBookStore.svelte";
import { ConsoleLogger }      from "@loggers/ConsoleLogger";
import { LibraryStore }       from "./stores/LibraryStore.svelte.ts";
import { VisibilityObserver } from "./utils/VisibilityObserver.ts";



export const logger              = new ConsoleLogger("DEBUG")
export const api_client          = new ApiClient(logger)
export const session_storage     = new SessionStorage()
export const session_manager     = new SessionManager(api_client, session_storage, logger)
export const manage_book_store   = new ManageBookStore(logger, api_client)
export const library_store       = new LibraryStore(logger, api_client)
export const visibility_observer = new VisibilityObserver()