import { session_manager }  from "@root/CompositionRoot";
import { browser }          from "$app/environment";



export const prerender = true;
export const ssr = false;

export const load = async () =>
{
    if (browser)
    {
        await session_manager.restoreSession()
    }
}