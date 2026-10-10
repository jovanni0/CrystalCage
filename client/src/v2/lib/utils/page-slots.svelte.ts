import { type Snippet } from "svelte"
import { getContext, 
         setContext }   from "svelte"



const PAGE_SLOTS_KEY = Symbol("page-slots")



export type PageSlotName = "topbar" | "sheets"



export class PageSlots
{
    public topbar: Snippet | null = $state(null)
    public sheets: Snippet | null = $state(null)


    /*
    *   Called by a layout that renders `Page` for its child pages.
    */
    public static providePageSlots(): PageSlots
    {
        return setContext(PAGE_SLOTS_KEY, new PageSlots())
    }


    /*
    *   Called by a page during initialisation, to hand its snippets to the layout's `Page`.
    *   Cleanup only clears a slot if it still holds this page's snippet, because the next page may already have filled it.
    */
    public static usePageSlots(snippets: Partial<Record<PageSlotName, Snippet>>): void
    {
        const slots   = getContext<PageSlots>(PAGE_SLOTS_KEY)
        const entries = Object.entries(snippets) as [PageSlotName, Snippet][]

        for (const [name, snippet] of entries)
        {
            slots[name] = snippet
        }

        $effect(() =>
        {
            return () =>
            {
                for (const [name, snippet] of entries)
                {
                    if (slots[name] === snippet)
                    {
                        slots[name] = null
                    }
                }
            }
        })
    }
}