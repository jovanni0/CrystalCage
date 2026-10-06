import { LibStore } from "@v2/lib/stores/library-store.svelte"



export const load = async ({ parent }) =>
{
    await parent()

    const library_store = new LibStore()
    await library_store.initialize()

    return { library_store }
}