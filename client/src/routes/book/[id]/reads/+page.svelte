<script lang="ts">
    import type { BookStore } from "@root/stores/BookStore.svelte";
    import      ReadListItem  from "./ReadListItem.svelte";
    import Button from "@root/components/button.svelte";
    import EditionSelectorSheet from "./EditionSelectorSheet.svelte";

    
    let { data } = $props()

    const { book_store }: { book_store: BookStore } = $derived(data)
    let dialog: ReturnType<typeof EditionSelectorSheet>
    let selected_edition_id: string | null = $state(null)

    async function confirmStartRead()
    {
        if (selected_edition_id === null) 
            return

        await book_store.startRead(selected_edition_id)
        selected_edition_id = null
        dialog.close()
    }
</script>



<div class = "content">
    {#if book_store.read_history.length === 0}
        <div class = "placeholder">
            <div class="info">
                <span class="main">No reads recorded yet.</span>
                <span>Here you will see your read history for this book.</span>
            </div>
            
            <Button 
                text="Start read" 
                type="primary" 
                onClick ={ () => dialog.open() }
            />
        </div>
    {:else}
        <div class = "start-reading">
            <span>N days total read time</span>
            <Button 
                text    = "Start Reading"
                onClick = { () => dialog.open() }
            />
        </div>
        {#each book_store.read_history as read, index (read.id)}
            <ReadListItem 
                record_id        = { read.id }
                read_number      = { index + 1 }
                start_date       = { read.start_date ?? "Unknown" }
                end_date         = { read.end_date ?? "Unknown" }
                notes            = { read.notes }
                edition_format   = { read.edition_format }
                edition_label    = { read.edition_name }
                edition_added_at = { read.edition_release_date }
                in_progress      = { read.start_date !== null && read.end_date === null }
                onFinish         = { () => book_store.finishRead(read.id) }
            />
        {/each}
    {/if}
</div>

<EditionSelectorSheet
    bind:this        = { dialog }
    editions         = { book_store.editions }
    bind:selected_id = { selected_edition_id }
    onConfirm        = { confirmStartRead }
/>




<style>
    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        padding: 16px;
        height: 100%;
    }

    .placeholder
    {
        display: flex;
        flex-direction: column;
        justify-content: center;
        gap: 16px;
        flex: 1;
    }

    .info {
        display: flex;
        flex-direction: column;
        gap: 12px;
        color: var(--text-secondary);
        letter-spacing: 0.05rem;
        text-align: center;
        padding: 32px 8px;
        padding-top: 64px;
    }

    .start-reading
    {
        display: flex;
        flex-direction: row;
        align-items: center;

        & span
        {
            flex: 1;
            margin-left: 0.5rem;
            color: var(--text-secondary);
        }
    }
</style>