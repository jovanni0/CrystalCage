<script lang="ts">
    import type { Snippet } from "svelte";
    import type { Tab } from "@v2/lib/rules/tab-rules";
    import type { BookProfileStore } from "@v2/lib/stores/book-profile-store.svelte";
    import type { OnNavigate }     from "@sveltejs/kit";
    import type { SwipeDirection } from "@v2/lib/utils/horizontal-swipe";
    import      { onNavigate }     from "$app/navigation";
    import      { horizontalSwipe } from "@v2/lib/utils/horizontal-swipe";
    import      { page } from "$app/state";
    import      { TabRules } from "@v2/lib/rules/tab-rules";
    import      Page from "@v2/lib/components/_other/page.svelte";
    import      Tabbar from "@v2/lib/components/bars/tabbar.svelte";
    import      TopbarBackAction from "@v2/lib/components/bars/topbar-back-action.svelte";
    import      IconButton from "@v2/lib/components/buttons/icon-button.svelte";
    import      BookEdit from "@v2/lib/components/icons/book-edit.svelte";
    import      CalendarPlus from "@v2/lib/components/icons/calendar-plus.svelte";
    import      ImageEdit from "@v2/lib/components/icons/image-edit.svelte";
    import { nav } from "@root/CompositionRoot";
    import { PageSlots } from "@v2/lib/utils/page-slots.svelte";
    import FilePlusCorner from "@v2/lib/components/icons/file-plus-corner.svelte";



    let {
        children,
        data
    }: {
        children?: Snippet,
        data:      { book_profile_store: BookProfileStore }
    } = $props()


    const slots = PageSlots.provide()

    const store           = $derived(data.book_profile_store)
    const tabs: Tab[]     = $derived([
        { id: "covers",   label: "Covers",   href: `/book-profile/${store.book_id}/covers` },
        { id: "book",     label: "Book",     href: `/book-profile/${store.book_id}/` },
        { id: "reads",    label: "Reads",    href: `/book-profile/${store.book_id}/reads` },
        { id: "editions", label: "Editions", href: `/book-profile/${store.book_id}/editions` },
    ])
    const current_page_id = $derived(TabRules.activeId(tabs, page.url.pathname))


    type TabDirection = "forward" | "backward"


    function tabIndexOf(pathname: string): number
    {
        const tab_id = TabRules.activeId(tabs, pathname)

        return tabs.findIndex(it => it.id === tab_id)
    }


    /*
    * `null` for anything that is not a switch between two tabs of this book (other books, other pages, same tab).
    */
    function tabDirectionOf(navigation: OnNavigate): TabDirection | null
    {
        if (navigation.from === null || navigation.to === null)
        {
            return null
        }

        const from_index = tabIndexOf(navigation.from.url.pathname)
        const to_index   = tabIndexOf(navigation.to.url.pathname)

        if (from_index === -1 || to_index === -1 || from_index === to_index)
        {
            return null
        }

        return to_index > from_index ? "forward" : "backward"
    }


    function prefersReducedMotion(): boolean
    {
        return window.matchMedia("(prefers-reduced-motion: reduce)").matches
    }


    /*
    * Tab switches (tap or swipe) slide the content in the direction of the new tab. `data-tab-direction` on <html> picks the
    * animation and scopes the view-transition CSS to these navigations only.
    */
    onNavigate((navigation) =>
    {
        const direction = tabDirectionOf(navigation)

        if (direction === null || document.startViewTransition === undefined || prefersReducedMotion())
        {
            return
        }

        return new Promise<void>((resolve) =>
        {
            const root = document.documentElement

            root.dataset.tabDirection = direction

            const transition = document.startViewTransition(async () =>
            {
                resolve()
                await navigation.complete
            })

            transition.finished.finally(() => delete root.dataset.tabDirection)
        })
    })


    function swipeToTab(direction: SwipeDirection): void
    {
        // the cover viewer and other overlays handle their own swipes
        if (nav.isOverlayOpen())
        {
            return
        }

        const next_tab = tabs[tabIndexOf(page.url.pathname) + direction]

        if (next_tab === undefined)
        {
            return
        }

        nav.goTo(next_tab.href, true)
    }

    let file_input: HTMLInputElement

    function onFilesPicked()
    {
        if (file_input.files)
        {
            store.stageFile(file_input.files[0])
            store.is_edition_add_sheet_open = true
        }

        file_input.value = ""
    }
</script>



<input
    type      = "file"
    accept    = ".epub,.mobi,.azw3,.fvb,.pdf,application/epub+zip,application/pdf"
    hidden
    bind:this = { file_input }
    onchange  = { onFilesPicked }
/>

<Page 
    class = "page-layout"
>
    {#snippet topbar()}
        <TopbarBackAction
            title = { store.title }
        >
            {#snippet action()}
                {#if current_page_id === "covers"}
                    <IconButton
                        shape = "square"
                        label = "manage covers"
                        onclick = { () => store.is_manage_cover_sheet_open = true }
                    >
                        <ImageEdit />
                    </IconButton>
                {:else if current_page_id === "book"}
                    <IconButton
                        shape = "square"
                        label = "edit book details"
                        onclick = { () => nav.goTo(`/add-book/${store.book_id}`) }
                    >
                        <BookEdit />
                    </IconButton>
                {:else if current_page_id === "reads"}
                    <IconButton
                        shape = "square"
                        label = "add new read"
                        onclick = { () => store.is_start_read_sheet_open = true }
                    >
                        <CalendarPlus />
                    </IconButton>
                {:else}
                    <IconButton
                        shape = "square"
                        label = "manage editions"
                        onclick = { () => file_input.click() }
                    >
                        <FilePlusCorner />
                    </IconButton>
                {/if}
            {/snippet}
        </TopbarBackAction>

        <Tabbar 
            tabs           = { tabs } 
            current_tab_id = { current_page_id }
            transition_name = "tab-indicator"
        />

        {#if store.is_reading}
            <p class = "in-progress-banner">In Progress</p>
        {/if}
    {/snippet}

    <tab-content {@attach horizontalSwipe(swipeToTab)}>
        {@render children?.()}
    </tab-content>

    {#snippet sheets()}
        {@render slots.contents.sheets?.()}
    {/snippet}
</Page>


<style>
    :global(layout.page-layout)
    {
        padding: 20px;
        view-transition-name: tab-content;
    }

    p.in-progress-banner
    {
        background-color: var(--bg-brand-secondary-muted);
        color: var(--text-brand-secondary);

        text-align: center;
        padding: 6px 0;

        font-size: var(--text-body-sm);
        font-weight: 600;
    }


    tab-content
    {
        display: flex;
        flex-direction: column;
        min-height: 100%;
    }


    /* everything below only applies while a tab switch is animating */

    :global(html[data-tab-direction]::view-transition-old(root)),
    :global(html[data-tab-direction]::view-transition-new(root))
    {
        animation: none;
    }

    :global(html[data-tab-direction]::view-transition-old(tab-content)),
    :global(html[data-tab-direction]::view-transition-new(tab-content))
    {
        animation-duration: 220ms;
        animation-timing-function: ease-out;
        mix-blend-mode: normal;
    }

    :global(html[data-tab-direction]::view-transition-group(tab-indicator))
    {
        animation-duration: 220ms;
        animation-timing-function: ease-out;
    }

    /* the snapshot must catch the indicator at its new position, not at the start of its own transition */
    :global(html[data-tab-direction] tab-indicator)
    {
        transition: none;
    }

    :global(html[data-tab-direction="forward"]::view-transition-old(tab-content))
    {
        animation-name: tab-slide-out-left;
    }

    :global(html[data-tab-direction="forward"]::view-transition-new(tab-content))
    {
        animation-name: tab-slide-in-right;
    }

    :global(html[data-tab-direction="backward"]::view-transition-old(tab-content))
    {
        animation-name: tab-slide-out-right;
    }

    :global(html[data-tab-direction="backward"]::view-transition-new(tab-content))
    {
        animation-name: tab-slide-in-left;
    }


    @keyframes -global-tab-slide-out-left
    {
        to
        {
            transform: translateX(-100%);
        }
    }

    @keyframes -global-tab-slide-in-right
    {
        from
        {
            transform: translateX(100%);
        }
    }

    @keyframes -global-tab-slide-out-right
    {
        to
        {
            transform: translateX(100%);
        }
    }

    @keyframes -global-tab-slide-in-left
    {
        from
        {
            transform: translateX(-100%);
        }
    }
</style>