import type { PageLoad }        from "./$types";
import      { session_manager } from "@root/CompositionRoot";
import      { redirect }        from "@sveltejs/kit";



export const load: PageLoad = () =>
{
    if (!session_manager.hasServerContext())
    {
        throw redirect(307, "/login/select-server")
    }
}