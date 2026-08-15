<script lang="ts">
    import type { ReadManagementStore } from "@root/stores/ReadManagementStore.svelte";
    import ListEntryDate from "@root/components/list-entry-date.svelte";
    import TopbarConfirmCancel from "@root/components/topbar-confirm-cancel.svelte";
    import ListEntryDropdown from "@components/list-entry-dropdown.svelte"
    import ListSingleItemEntry from "@root/components/list-single-item-entry.svelte";
    import { goBack } from "@root/utils/go-back";
    import { marked } from "marked";

    let { data } = $props()
        const { 
        read_store, 
        id 
    } : { 
        read_store: ReadManagementStore
        id: string | undefined 
    } = $derived(data)


    async function save()
    {
        const has_saved = await read_store.save()

        if (has_saved)
        {
            goBack()
        }
    }
</script>



<TopbarConfirmCancel 
    title = "Edit Read #"
    onCancel = { goBack }
    onConfirm = { save }
/>
<div class = "content">
    <div class = "book-info">
        <span class = "title">{ read_store.book_title }</span>
        <span class = "author">by { read_store.author_name }</span>
    </div>

    <div class = "list">
        <span class="heading">DETAILS</span>

        <ListEntryDate 
            key = "Started"
            bind:value = { 
                () => read_store.start_date ?? undefined,
                (value) => read_store.start_date = value ?? null
            }
        />

        <ListEntryDate 
            key = "Finished"
            bind:value = { 
                () => read_store.end_date ?? undefined,
                (value) => read_store.end_date = value ?? null
            }
        />
        <ListEntryDropdown 
            key = "Edition"
            options = { read_store.edition_options }
            bind:selected_id = { read_store.selected_edition_id }
        />
    </div>

    {#if read_store.notes}
        <ListSingleItemEntry
            key         = "NOTES"
            href        = { `/manage-reads/${id}/notes` }
            hasContent  = { true }
        >
            {@html marked(read_store.notes)}
        </ListSingleItemEntry>
    {/if}
</div>



<style>
    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;
    }

    .book-info 
    {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
        text-align: center;

        & .title
        {
            font-weight: 500;
        }

        & .author
        {
            color: var(--text-secondary);
        }
    }

    .heading {
        padding: 0 8px;
        font-weight: 700;
        color: var(--text-secondary);
        letter-spacing: 0.05em;
    }

    .list {
        display: flex;
        flex-direction: column;
    }
</style>