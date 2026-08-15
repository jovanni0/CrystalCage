<script lang="ts">
    import type { ReadManagementStore } from "@root/stores/ReadManagementStore.svelte";
    import ListEntryDate from "@root/components/list-entry-date.svelte";
    import TopbarConfirmCancel from "@root/components/topbar-confirm-cancel.svelte";
    import ListEntryDropdown from "@components/list-entry-dropdown.svelte"
    import ListSingleItemEntry from "@root/components/list-single-item-entry.svelte";
    import { goBack } from "@root/utils/go-back";
    import { marked } from "marked";
    import TopbarBack from "@root/components/topbar-back.svelte";
    import Pencil from "@root/icons/Pencil.svelte";
    import StatRow from "@root/components/StatRow.svelte";
    import ListEntryText from "@root/components/list-entry-text.svelte";
    import ListText from "@root/components/ListText.svelte";

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



<TopbarBack 
    page_name = "Read #"
>
    {#snippet right()}
        <a href = { `/manage-read/${id}` }>
            <Pencil />
        </a>
    {/snippet}
</TopbarBack>

<div class = "content">
    <div class = "book-info">
        <span class = "title">{ read_store.book_title }</span>
        <span class = "author">by { read_store.author_name }</span>
    </div>
    
    <StatRow 
        stats = {[
            { label: "started", value: read_store.start_date ?? "Unknown" },
            { label: "finished", value: read_store.end_date ?? "Unknown" }
        ]}
        centered = { false }
    />

    <div class = "list">
        {#if read_store.total_time !== null}
            <ListText 
                key   = "Total Time"
                value = { `${read_store.total_time.toString()} days` }
            />
        {/if}

        {#if read_store.pace !== null}
            <ListText 
                key   = "Pace"
                value = { `${read_store.pace.toString()} w/d` }
            />
        {/if}

        <ListText 
            key   = "Edition"
            value = { `${read_store.edition_label} (${read_store.edition_format.toUpperCase()})` }
        />
    </div>

    {#if read_store.notes}
        {@html marked(read_store.notes)}
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

    .list {
        display: flex;
        flex-direction: column;
    }
</style>