<script lang="ts">
    import Page from "@v2/lib/components/_other/page.svelte"
    import CoverWrapper from "@v2/lib/components/covers/cover-wrapper.svelte";
    import TopbarBrandSearch from "@v2/lib/components/bars/topbar-brand-search.svelte";
    import type { LibStore } from "@v2/lib/stores/library-store.svelte.js";
    import Navbar from "@v2/lib/components/bars/navbar.svelte";
    import CurrentlyReadingCarusel from "@v2/lib/elements/currently-reading-carusel.svelte";
    import FilterChipBar from "@v2/lib/components/_other/filter-chip-bar.svelte";
    import UpdateProgressSheet from "@v2/lib/elements/update-progress-sheet.svelte";
    import { nav } from "@root/CompositionRoot.js";


    let { data } = $props()
    const { library_store } : { library_store: LibStore } = $derived(data)

    const filter_options = ["All", "Gallery", "DNFs", "To Read", "Waitlist", "Favorites"]

    let filter: string = $state("")
</script>



<Page 
    class = "library-page-content"
>
    {#snippet topbar()}
        <TopbarBrandSearch 
            onAddBook = { () => nav.goTo("/add-book")}
        />
    {/snippet}

    <filter-wrapper>
        <FilterChipBar 
            options    = { filter_options }
            bind:value = { filter }
            name       = "library filters"
            label      = "library filters"
        />
    </filter-wrapper>

    {#if library_store.current_reads.length > 0}
        <CurrentlyReadingCarusel 
            items = { library_store.current_reads }
            triggerSheet = { (id: string) => library_store.openProgressSheet(id) }
        />
    {/if}

    <cover-grid>
        {#each library_store.items as item (item.book_id)}
            <CoverWrapper 
                book_id    = { item.book_id }
                cover_id   = { item.cover_id }
                cover_type = "thumbnail"
                alt        = { item.label }
                label      = { item.label }
            />
        {/each}
    </cover-grid>

    {#snippet navbar()}
        <Navbar />
    {/snippet}

    {#snippet sheets()}
        <UpdateProgressSheet
            bind:is_open = { library_store.is_progress_sheet_open }
            read         = { library_store.selected_read }
            is_saving    = { library_store.is_saving_progress }
            error        = { library_store.error_message }
            onSave       = { (progress: number) => library_store.saveProgress(progress) }
        />
    {/snippet}
</Page>


<style>
    :global(layout.library-page-content)
    {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    filter-wrapper
    {
        padding: 0 16px;
    }


    cover-grid
    {
        display: grid;
        grid-template-columns: repeat(auto-fill, 112px);
        justify-content: space-between;
        align-items: baseline;
        column-gap: 8px;
        row-gap: 12px;
        padding: 8px 24px;
    }
</style>