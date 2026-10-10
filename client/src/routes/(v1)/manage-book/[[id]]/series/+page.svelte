<script lang="ts">
    import type { BookManagementStore } from "@root/stores/BookManagementStore.svelte.js";
    import      { logger }              from "@root/CompositionRoot";
    import      FieldAddSeach           from "@root/components/field-add-seach.svelte";
    import      ListSelectableRadio     from "@root/components/list-selectable-radio.svelte";
    import      TopbarBack              from "@root/components/topbar-back.svelte";


    let { data } = $props()
    const { book_store } : { book_store: BookManagementStore } = $derived(data)

    async function createSeries()
    {
        const author_id = book_store.author_id
        const universe_id = book_store.universe_id
        const series_name = book_store.series_search

        if (!author_id)
        {
            logger.warn(`trying to create a series with no author id`)
            return
        }

        const is_created = await book_store.createSeries(series_name, author_id, universe_id)

        if (!is_created)
        {
            logger.warn(`could not create series`, { name: series_name, author: author_id, universe: universe_id })
            return
        }

        book_store.series_search = ""
    }
</script>



<TopbarBack 
    page_name = "Select Series"
/>

<div class="body">
    <div class="search-wrapper">
        <FieldAddSeach 
            bind:value  = { book_store.series_search }
            placeholder = "Search or add series..."
        />
        
        {#if !book_store.is_author_selected}
            <span class="warning">You need to first select or create an author!</span>
        {/if}
    </div>

    {#if book_store.series_results.length > 0}
        <div class="options">
            {#each book_store.series_results as series (series.id)}
                <ListSelectableRadio 
                    label    = { series.name }
                    name     = "series"
                    value    = { series.id }
                    checked  = { book_store.series_id === series.id }
                    onchange = { (id) => book_store.setSeriesId(id) }
                />
            {/each}
        </div>
    {/if}

    {#if book_store.is_author_selected && book_store.series_search !== ""}
        <button 
            class   = "create"
            onclick = { createSeries }
        >
            <span>Create</span>
            <span class="new-value">{ book_store.series_search.trim() }</span>
        </button>
    {/if}
</div>



<style>
    .body {
        display: flex;
        flex-direction: column;
        gap: 20px;
        padding: 16px;
    }

    .search-wrapper {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    .options {
        display: flex;
        flex-direction: column;
    }

    .create {
        display: flex;
        flex-direction: row;
        gap: 8px;
        padding: 12px 16px;
        border-radius: 12px;
        background-color: var(--raised);
        border: 1px solid var(--raised-border);
        align-self: stretch;
    }

    .new-value {
        color: var(--primary-text);
        font-weight: 600;
    }

    .warning {
        color: var(--red-primary);
        font-size: small;
        padding: 0 8px;
    }
</style>
