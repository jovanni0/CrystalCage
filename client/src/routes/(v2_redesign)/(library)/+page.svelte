<script lang="ts">
    import Page from "@v2/lib/components/_other/page.svelte"
    import CoverWrapper from "@v2/lib/components/covers/cover-wrapper.svelte";
    import TopbarBrandSearch from "@v2/lib/components/bars/topbar-brand-search.svelte";
    import type { LibStore } from "@v2/lib/stores/library-store.svelte.js";
    import Navbar from "@v2/lib/components/bars/navbar.svelte";
    import CurrentlyReadingCarusel from "@v2/lib/elements/currently-reading-carusel.svelte";
    import FilterChipBar from "@v2/lib/components/_other/filter-chip-bar.svelte";


    let { data } = $props()
    const { library_store } : { library_store: LibStore } = $derived(data)

    const filter_options = ["All", "Gallery", "DNFs", "To Read", "Waitlist", "Favorites"]

    let filter: string = $state("")
</script>



<Page class = "library-page-content">
    {#snippet topbar()}
        <TopbarBrandSearch />
    {/snippet}

    <filter-wrapper>
        <FilterChipBar 
            options    = { filter_options }
            bind:value = { filter }
            name       = "library filters"
            label      = "library filters"
        />
    </filter-wrapper>

    {#if library_store.are_current_reads}
        <CurrentlyReadingCarusel 
            items = { library_store.currently_reading }
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
</Page>


<style>
    :global(layout.library-page-content)
    {
        display: flex;
        flex-direction: column;
    }

    filter-wrapper
    {
        padding: 8px 16px;
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