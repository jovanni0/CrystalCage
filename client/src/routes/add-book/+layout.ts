import { manage_book_store } from "@root/CompositionRoot"
import { redirect } from "@sveltejs/kit"



export const load = async ({ url }) =>
{
    if (url.pathname === "/add-book" && manage_book_store.is_initialized == false)
    {
        manage_book_store.initialize()

        return
    }

    if (!manage_book_store.is_initialized)
    {
        throw redirect(307, "/add-book")
    }
}