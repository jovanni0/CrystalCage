import { library_store } from "@root/CompositionRoot"



export const load = async ({ parent }) =>
{
    await parent()
    
    await library_store.initialize()
}