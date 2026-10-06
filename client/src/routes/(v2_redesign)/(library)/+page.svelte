<script lang="ts">
    import Page from "@v2/lib/components/_other/page.svelte"
    import CoverWrapper from "@v2/lib/components/covers/cover-wrapper.svelte";
    import TopbarBrandSearch from "@v2/lib/components/bars/topbar-brand-search.svelte";
    import type { LibStore } from "@v2/lib/stores/library-store.svelte.js";
    import Navbar from "@v2/lib/components/bars/navbar.svelte";


    let { data } = $props()
    const { library_store } : { library_store: LibStore } = $derived(data)
</script>



<Page class = "library-page-content">
    {#snippet topbar()}
        <TopbarBrandSearch />
    {/snippet}

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
        padding: 8px 24px;
    }


    cover-grid
    {
        display: grid;
        grid-template-columns: repeat(auto-fill, 112px);
        justify-content: space-between;
        align-items: baseline;
        column-gap: 8px;
        row-gap: 12px;
    }
</style>