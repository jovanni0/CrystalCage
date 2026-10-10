import { getContext, setContext, type Snippet } from "svelte"



const PAGE_SLOTS_KEY = Symbol("page-slots")



/*
*   Everything a page can hand to the layout's `Page`. Add new slots here; nothing else needs to change.
*/
export type PageSlotContents = {
    topbar:  Snippet
    sheets:  Snippet
    refresh: () => Promise<void>
}


export type PageSlotName = keyof PageSlotContents



export class PageSlots
{
    public contents: Partial<PageSlotContents> = $state({})


    /*
    *   Called by a layout that renders `Page` for its child pages.
    */
    public static provide(): PageSlots
    {
        return setContext(PAGE_SLOTS_KEY, new PageSlots())
    }


    /*
    *   Called by a page during initialisation, to hand its contents to the layout's `Page`.
    *   Cleanup only clears a slot if it still holds this page's value, because the next page may already have filled it.
    */
    public static use(contents: Partial<PageSlotContents>): void
    {
        const slots = getContext<PageSlots | undefined>(PAGE_SLOTS_KEY)

        if (slots === undefined)
        {
            throw new Error("PageSlots.use() needs a layout that calls PageSlots.provide()")
        }

        const names = Object.keys(contents) as PageSlotName[]

        Object.assign(slots.contents, contents)

        $effect(() =>
        {
            return () =>
            {
                for (const name of names)
                {
                    if (slots.contents[name] === contents[name])
                    {
                        delete slots.contents[name]
                    }
                }
            }
        })
    }
}