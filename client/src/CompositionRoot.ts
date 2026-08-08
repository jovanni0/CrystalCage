import {ApiClient} from "./utils/ApiClient.ts";
import {SessionStorage} from "./utils/SessionStorage.ts";
import {SessionManager} from "./utils/SessionManager.svelte.ts";



export const api_client = new ApiClient()
export const session_storage = new SessionStorage()
export const session_manager = new SessionManager(api_client, session_storage)