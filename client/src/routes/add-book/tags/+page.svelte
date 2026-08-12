<script lang="ts">
    import FieldAddSeach          from "@root/components/field-add-seach.svelte";
    import ListSelectableCheckbox from "@root/components/list-selectable-checkbox.svelte";
    import TopbarBack             from "@root/components/topbar-back.svelte";
    import { logger, manage_book_store } from "@root/CompositionRoot";



    async function createTag(): Promise<void>
    {
        const tag_name = manage_book_store.tag_search
        const is_created = await manage_book_store.createTag(tag_name, null)

        if (!is_created)
        {
            logger.warn(`could not create tag`, { name: tag_name })
            return
        }

        manage_book_store.tag_search = ""
    }
</script>



<TopbarBack 
    page_name = "Select Tags"
/>

<div class="body">
    <FieldAddSeach 
        bind:value  = { manage_book_store.tag_search }
        placeholder = "Search or add tag..."
    />

    {#if manage_book_store.tag_results.length > 0}
        <div class="options">
            {#each manage_book_store.tag_results as tag (tag.id)}
                <ListSelectableCheckbox 
                    label    = { tag.name }
                    value    = { tag.id }
                    checked  = { manage_book_store.tag_ids.includes(tag.id) ?? false }
                    onchange = { (id, selected) => manage_book_store.toggleTagId(id, selected) }
                />
            {/each}
        </div>
    {/if}

    {#if manage_book_store.tag_search !== ""}
        <button 
            class   = "create"
            onclick = { createTag }
        >
            <span>Create</span>
            <span class="new-value">{ manage_book_store.tag_search.trim() }</span>
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
</style>
