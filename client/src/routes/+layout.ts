import { browser }          from "$app/environment";
import { redirect }         from "@sveltejs/kit";
import { toSearchParams }   from "@root/utils/SearchParams";
import { api_auth, 
         logger, 
         session }          from "@root/CompositionRoot";
import { attempt }          from "@v2/lib/utils/result";



export const prerender = true;
export const ssr = false;



export const load = async ({ route }) =>
{
    if (!browser)
    {
        return
    }

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

            // If the error is 401 `ApiClient` will have called `signOut()`.
            //
            // If there are network errors keep the token and do nothing, the app still works with it's cached data.
        }
    }

    const is_onboarding = route.id?.includes("onboarding") ?? false

    if (session.status === "AUTHENTICATED")
    {
        if (is_onboarding)
        {
            redirect(303, "/")
        }

        return
    }

    if (is_onboarding) return

    if (session.status === "NEEDS_LOGIN")
    {
        const search_params = toSearchParams({ 
            address: session.base_url, 
            name:    session.server_name,
        })

        redirect(303, search_params ? `/login?${search_params}`: "/auto-discovery")
    }

    redirect(303, "/welcome")
}