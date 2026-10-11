<script lang="ts">
    import type { LibStore }            from "@v2/lib/stores/library-store.svelte.js";
    import      { PageSlots }           from "@v2/lib/utils/page-slots.svelte.js";
    import      { nav }                 from "@root/CompositionRoot.js";
    import      CoverWrapper            from "@v2/lib/components/covers/cover-wrapper.svelte";
    import      CurrentlyReadingCarusel from "@v2/lib/elements/currently-reading-carusel.svelte";
    import      FilterChipBar           from "@v2/lib/components/_other/filter-chip-bar.svelte";
    import      UpdateProgressSheet     from "@v2/lib/elements/update-progress-sheet.svelte";
    import      TopbarBrandSearch       from "@v2/lib/components/bars/topbar-brand-search.svelte";
    import { hasContext } from "svelte";
    import SearchX from "@v2/lib/components/icons/search-x.svelte";
    import ShelvingUnit from "@v2/lib/components/icons/shelving-unit.svelte";
    import LinkButton from "@v2/lib/components/buttons/link-button.svelte";



    let { data } = $props()
    const { library_store } : { library_store: LibStore } = $derived(data)
    
    PageSlots.use({ 
        sheets, 
        topbar,
        refresh: () => library_store.refresh()
    })
</script>



<library-page-wrapper>
    <filter-wrapper>
        <FilterChipBar 
            options    = { library_store.filters.map(it => ({ id: it.id, label: it.name })) }
            bind:value = { library_store.active_filter_id }
            name       = "library filters"
            label      = "library filters"
        />
    </filter-wrapper>
    
    {#if library_store.has_items && library_store.items.length === 0}
        <empty-wrapper>
            <SearchX 
                width  = { 80 }
                height = { 80 }
            />
            <p class = "empty-message">No books match the current filters.</p>
        </empty-wrapper>
    {:else if library_store.has_items}
        <CurrentlyReadingCarusel 
            items        = { library_store.current_reads }
            triggerSheet = { (id: string) => library_store.openProgressSheet(id) }
        />

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
    {:else}
        <empty-wrapper>
            <ShelvingUnit
                width  = { 80 }
                height = { 80 }
            />

            <p class = "empty-message">Your library is empty.<br>Add a book to get started.</p>

            <LinkButton
                href = "/add-book"
            >
                Add yout first book
            </LinkButton>
        </empty-wrapper>
    {/if}
</library-page-wrapper>

{#snippet topbar()}
    <TopbarBrandSearch 
        onAddBook = { () => nav.goTo("/add-book")}
    />
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



<style>
    library-page-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 16px;
        padding-top: 8px;

        min-height: 98%;
    }

    filter-wrapper
    {
        display: flex;
        padding: 0 20px;
    }


    cover-grid
    {
        display: grid;
        grid-template-columns: repeat(auto-fill, 112px);
        justify-content: space-between;
        align-items: start;
        column-gap: 8px;
        row-gap: 12px;
        padding: 0 20px;
    }

    empty-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 16px;
        flex: 1;
        align-self: stretch;
        color: var(--text-brand-secondary);

        & > :global(svg *)
        {
            stroke-width: 3;
            vector-effect: non-scaling-stroke;
        }

        & > :global(a)
        {
            margin-top: 16px;
        }

        p.empty-message
        {
            color: var(--text);
            text-align: center;
        }
    }
</style>