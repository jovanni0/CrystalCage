import { redirect }     from "@sveltejs/kit";
import { api_auth, 
         logger, 
         session }      from "@root/CompositionRoot";
import { attempt }      from "@v2/lib/utils/result";
import { SessionRules } from "@v2/lib/rules/session-rules";



export const prerender = true;
export const ssr = false;



/*
* Runs once: restores the session and redirects the first page if needed.
*
* `route` is read inside `untrack`, so this load does not rerun on navigation. If it did, every child layout that awaits `parent()`
* would rerun too and rebuild its store, losing unsaved state. Redirects on later navigations happen in `+layout.svelte`.
*/
export const load = async ({ route, untrack }) =>
{
    if (session.status === "UNKNOWN")
    {
        const status = await session.restore()

        if (status === "AUTHENTICATED")
        {
            const me = await attempt(logger, "verify session", () => api_auth.me())

            if (me.ok)
            {
                session.user = me.value
            }

            // on 401 `SessionHttp` has already signed out; on network errors the token is kept and cached data is used
        }
    }

    const route_id = untrack(() => route.id)
    const target   = SessionRules.redirectFor(session.status, route_id, session.base_url, session.server_name)

    if (target !== null)
    {
        redirect(303, target)
    }
}
