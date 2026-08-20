import { session_manager }  from "@root/CompositionRoot";
import { browser }          from "$app/environment";



export const prerender = true;
export const ssr = false;

// NOTE: the previous code was triggering all the `load` functions to run when navigating from a page to a child page,
// so the objects constructed on other `load` functions did not persist. this fixes it, but might create bugs with session management.
export const load = async () =>
{
    if (browser)
    {
        await session_manager.restoreSession()
    }
}