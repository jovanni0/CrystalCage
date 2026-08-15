<script lang="ts">
    import type { BookManagementStore } from "@root/stores/BookManagementStore.svelte.js";
    import      { logger }              from "@root/CompositionRoot";
    import      FieldAddSeach           from "@root/components/field-add-seach.svelte";
    import      ListSelectableRadio     from "@root/components/list-selectable-radio.svelte";
    import      TopbarBack              from "@root/components/topbar-back.svelte";


    let { data } = $props()
    const { book_store } : { book_store: BookManagementStore } = $derived(data)

    async function createAuthor(): Promise<void>
    {
        const full_name = book_store.author_search

        const is_created = await book_store.createAuthor(full_name)

        if (!is_created)
        {
            logger.warn(`could not create author`, { author: full_name })
            return
        }

        book_store.author_search = ""
    }
</script>



<TopbarBack 
    page_name = "Select Author"
/>

<div class="body">
    <FieldAddSeach 
        bind:value  = { book_store.author_search }
        placeholder = "Search or add author..."
    />

    {#if book_store.author_results.length > 0}
        <div class="options">
            {#each book_store.author_results as author (author.id)}
                <ListSelectableRadio 
                    label    = {`${author.last_name}, ${author.first_name}`}
                    name     = "author"
                    value    = {author.id}
                    checked  = {book_store.author_id === author.id}
                    onchange = { (id) => book_store.setAuthorId(id) }
                />
            {/each}
        </div>
    {/if}

    {#if book_store.author_search !== ""}
        <div class="new-option">
            <button 
                class   = "create"
                onclick = { createAuthor }
            >
                <span>Create</span>
                <span class="new-value">{ book_store.author_search.trim() }</span>
            </button>
            
            {#if !book_store.author_search.includes(',')}
                <span class="warning">Make sure the format is <b>Last, First Name</b> separated with a colon (,) otherwise some features may break.</span>
            {/if}
        </div>
    {/if}
</div>



<style>
    .body {
        display: flex;
        flex-direction: column;
        gap: 20px;
        padding: 16px;
    }

    .options,
    .new-option {
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
        font-size: small;
        color: var(--text-secondary);
        padding: 0 8px;
    }
</style>