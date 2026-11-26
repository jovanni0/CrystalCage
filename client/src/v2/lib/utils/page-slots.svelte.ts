import { getContext, setContext, type Snippet } from "svelte"



const PAGE_SLOTS_KEY = Symbol("page-slots")



export type PageSlotContents = {
    topbar:  Snippet
    sheets:  Snippet
    refresh: () => Promise<void>
}


export type PageSlotName = keyof PageSlotContents


type SlotEntry =
{
    contents: Partial<PageSlotContents>
}



export class PageSlots
{
    /*
    *   What the layout renders: the slots of every registered page merged, later pages winning.
    */
    public contents: Partial<PageSlotContents> = $state.raw({})

    /*
    *   Not reactive on purpose. In Svelte 5, state read inside an effect teardown returns the value from before the current
    *   update, so a page leaving during a navigation would see its own contents and remove the next page's. A plain list
    *   gives each page an exact record to remove, whatever order pages mount and unmount in.
    */
    private entries: SlotEntry[] = []


    /*
    *   Called by a layout that renders `Page` for its child pages.
    */
    public static provide(): PageSlots
    {
        return setContext(PAGE_SLOTS_KEY, new PageSlots())
    }


    /*
    *   Called by a page during initialisation, to hand its contents to the layout's `Page`.
    */
    public static use(contents: Partial<PageSlotContents>): void
    {
        const slots = getContext<PageSlots | undefined>(PAGE_SLOTS_KEY)

        if (slots === undefined)
        {
            throw new Error("PageSlots.use() needs a layout that calls PageSlots.provide()")
        }

        const entry: SlotEntry = { contents }

        slots.entries = [ ...slots.entries, entry ]
        slots.publish()

        $effect(() =>
        {
            return () =>
            {
                slots.entries = slots.entries.filter(it => it !== entry)
                slots.publish()
            }
        })
    }


    private publish(): void
    {
        const merged: Partial<PageSlotContents> = {}

        for (const entry of this.entries)
        {
            Object.assign(merged, entry.contents)
        }

        this.contents = merged
    }
}