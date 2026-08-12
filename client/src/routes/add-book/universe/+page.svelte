<script lang="ts">
    import FieldAddSeach       from "@root/components/field-add-seach.svelte";
    import ListSelectableRadio from "@root/components/list-selectable-radio.svelte";
    import TopbarBack          from "@root/components/topbar-back.svelte";
    import { logger, manage_book_store } from "@root/CompositionRoot";


    async function createUniverse(): Promise<void>
    {
        const author_id = manage_book_store.author_id
        const universe_name = manage_book_store.universe_search

        if (!author_id)
        {
            logger.warn("trying to create universe without an author selected")
            return
        }

        const is_created = await manage_book_store.createUniverse(author_id, universe_name)

        if (!is_created)
        {
            logger.warn(`could not create universe`, { name: universe_name, author: author_id })
            return
        }

        manage_book_store.series_search = ""
    }
</script>



<TopbarBack 
    page_name = "Select Universe"
/>

<div class="body">
    <div class="search-wrapper">
        <FieldAddSeach 
            bind:value  = { manage_book_store.universe_search }
            placeholder = "Search or add universe..."
        />
        
        {#if !manage_book_store.is_author_selected}
            <span class="warning">You need to first select or create an author!</span>
        {/if}
    </div>

    {#if manage_book_store.universe_results.length > 0}
        <div class="options">
            {#each manage_book_store.universe_results as universe (universe.id)}
                <ListSelectableRadio 
                    label    = { universe.name }
                    name     = "universe"
                    value    = { universe.id }
                    checked  = { manage_book_store.universe_id === universe.id }
                    onchange = { (id) => manage_book_store.setUniverseId(id) }
                />
            {/each}
        </div>
    {/if}

    {#if manage_book_store.is_author_selected && manage_book_store.universe_search !== ""}
        <button 
            class   = "create"
            onclick = { createUniverse }
        >
            <span>Create</span>
            <span class="new-value">{ manage_book_store.universe_search.trim() }</span>
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