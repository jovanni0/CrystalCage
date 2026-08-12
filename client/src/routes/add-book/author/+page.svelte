<script lang="ts">
    import FieldAddSeach        from "@root/components/field-add-seach.svelte";
    import ListSelectableRadio  from "@root/components/list-selectable-radio.svelte";
    import TopbarBack           from "@root/components/topbar-back.svelte";
    import { logger, manage_book_store } from "@root/CompositionRoot";



    async function createAuthor(): Promise<void>
    {
        const full_name = manage_book_store.author_search

        const is_created = await manage_book_store.createAuthor(full_name)

        if (!is_created)
        {
            logger.warn(`could not create author`, { author: full_name })
            return
        }

        manage_book_store.author_search = ""
    }
</script>



<TopbarBack 
    page_name = "Select Author"
/>

<div class="body">
    <FieldAddSeach 
        bind:value  = { manage_book_store.author_search }
        placeholder = "Search or add author..."
    />

    {#if manage_book_store.author_results.length > 0}
        <div class="options">
            {#each manage_book_store.author_results as author (author.id)}
                <ListSelectableRadio 
                    label    = {`${author.last_name}, ${author.first_name}`}
                    name     = "author"
                    value    = {author.id}
                    checked  = {manage_book_store.author_id === author.id}
                    onchange = { (id) => manage_book_store.setAuthorId(id) }
                />
            {/each}
        </div>
    {/if}

    {#if manage_book_store.author_search !== ""}
        <div class="new-option">
            <button 
                class   = "create"
                onclick = { createAuthor }
            >
                <span>Create</span>
                <span class="new-value">{ manage_book_store.author_search.trim() }</span>
            </button>
            
            {#if !manage_book_store.author_search.includes(',')}
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