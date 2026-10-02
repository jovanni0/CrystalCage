import { session_manager }  from "@root/CompositionRoot";
import { browser }          from "$app/environment";
import { redirect }         from "@sveltejs/kit";
import { toSearchParams }   from "@root/utils/SearchParams";



export const prerender = true;
export const ssr = false;

// NOTE: the previous code was triggering all the `load` functions to run when navigating from a page to a child page,
// so the objects constructed on other `load` functions did not persist. this fixes it, but might create bugs with session management.
export const load = async ({ route }) =>
{
    if (!browser)
    {
        return
    }

    if (session_manager.status === "UNKNOWN")
    {
        await session_manager.restoreSession()
    }

    const status = session_manager.status
    const is_onboarding = route.id?.includes("onboarding") ?? false

    if (status === "AUTHENTICATED" && is_onboarding) 
    {
        redirect(303, "/")
    } 
    else if (status === "NEEDS_LOGIN" && !is_onboarding) 
    {
        const search_params = toSearchParams({ 
            address: session_manager.server_address, 
            name:    session_manager.server_name,
        })

        if (search_params)
        {
            redirect(303, `/login?${search_params}`)
        }
        else
        {
            redirect(303, "/auto-discovery")
        }
    } 
    else if (status === "NO_SERVER" && !is_onboarding) 
    {
        redirect(303, "/welcome")
    }
}