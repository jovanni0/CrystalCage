<script lang="ts">
    import type { BookStore }        from "@root/stores/BookStore.svelte";
    import      ReadListItem         from "./ReadListItem.svelte";
    import      Button               from "@root/components/button.svelte";
    import      EditionSelectorSheet from "./EditionSelectorSheet.svelte";
    import      ReadDataSheet        from "./ReadDataSheet.svelte";

    
    let { data } = $props()

    const { book_store }: { book_store: BookStore } = $derived(data)
    let data_dialog: ReturnType<typeof ReadDataSheet>
    let editions_dialog: ReturnType<typeof EditionSelectorSheet>

    let selected_edition_id: string | null = $state(null)
    let start_date: string | null = $state("")
    let end_date:   string | null = $state("")
    let is_historical: boolean = $state(false)
    let did_not_finish: boolean = $state(false)


    function initializeStartRead()
    {
        clearFields()
        data_dialog.open()
    }


    async function finishCollectingData()
    {
        if (book_store.editions.length > 0)
        {
            editions_dialog.open()
            return
        }

        const is_good = await book_store.startRead(null, start_date || null, end_date || null, did_not_finish, is_historical)

        if (is_good) 
        {
            data_dialog.close()
            clearFields()
        }
    }


    async function finishSelectingEdition()
    {
        if (selected_edition_id === null)
            return

        const is_good = await book_store.startRead(selected_edition_id, start_date || null, end_date || null, did_not_finish, is_historical)

        if (is_good)
        {
            editions_dialog.close()
            clearFields()
        }
    }


    function clearFields()
    {
        selected_edition_id = null
        start_date = ""
        end_date = ""
        is_historical = false
        did_not_finish = false
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
                onClick ={ initializeStartRead }
            />
        </div>
    {:else}
        {#if book_store.can_start_read}
            <div class = "start-reading">
                <span>{ book_store.total_read_time } days total read time</span>

                <Button 
                    text    = "Start Reading"
                    onClick = { initializeStartRead }
                />
            </div>
        {/if}

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


<ReadDataSheet 
    bind:this = { data_dialog }
    bind:start_date = { start_date }
    bind:end_date = { end_date }
    bind:is_historical = { is_historical }
    bind:did_not_finish = { did_not_finish }
    allow_historical = { !book_store.has_historical }
    onConfirm = { finishCollectingData }
/>

<EditionSelectorSheet
    bind:this        = { editions_dialog }
    editions         = { book_store.editions }
    bind:selected_id = { selected_edition_id }
    onConfirm        = { finishSelectingEdition }
    button_text      = { book_store.editions.length > 0 ? "Continue" : "Start reading" }
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