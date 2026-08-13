import { manage_book_store, session_manager } from "@root/CompositionRoot"
import { redirect } from "@sveltejs/kit"



export const load = async ({ url, parent }) =>
{
    await parent()

    if (session_manager.status !== "AUTHENTICATED")
    {
        return
    }

    if (url.pathname === "/add-book" && manage_book_store.is_initialized == false)
    {
        await manage_book_store.initialize()

        return
    }

    if (!manage_book_store.is_initialized)
    {
        throw redirect(307, "/add-book")
    }
}