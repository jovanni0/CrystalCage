<script lang="ts">
    import type { Snippet } from "svelte";
    import type { Tab } from "@v2/lib/rules/tab-rules";
    import type { BookProfileStore } from "@v2/lib/stores/book-profile-store.svelte";
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

    console.log(store.editions)
</script>



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
                        onclick = { () => nav.goTo(`/manage-covers/${store.book_id}`) }
                    >
                        <ImageEdit />
                    </IconButton>
                {:else if current_page_id === "details"}
                    <IconButton
                        shape = "square"
                        label = "edit book details"
                        onclick = { () => nav.goTo(`/add-book/${store.cover_id}`) }
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
                        onclick = { () => nav.goTo(`/manage-editions/${store.book_id}`) }
                    >
                        <BookEdit />
                    </IconButton>
                {/if}
            {/snippet}
        </TopbarBackAction>

        <Tabbar 
            tabs           = { tabs } 
            current_tab_id = { current_page_id }
        />

        {#if store.is_reading}
            <p class = "in-progress-banner">In Progress</p>
        {/if}
    {/snippet}

    {@render children?.()}

    {#snippet sheets()}
        {@render slots.contents.sheets?.()}
    {/snippet}
</Page>


<style>
    :global(layout.page-layout)
    {
        padding: 20px;
    }

    p.in-progress-banner
    {
        background-color: var(--bg-brand-secondary-muted);
        color: var(--text-brand-secondary);

        text-align: center;
        padding: 6px 0;

        font-size: var(--text-body-small);
        font-weight: 600;
    }
</style>