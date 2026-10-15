<script lang="ts">
    import { nav } from '@root/CompositionRoot';
    import Button from '@v2/lib/components/buttons/button.svelte';
    import CalendarX from '@v2/lib/components/icons/calendar-x.svelte';
    import ReadRecordPreview from '@v2/lib/elements/read-record-preview.svelte';
    import StartReadSheet from '@v2/lib/elements/start-read-sheet.svelte';
    import type { BookProfileStore } from '@v2/lib/stores/book-profile-store.svelte';
    import { PageSlots } from '@v2/lib/utils/page-slots.svelte';


    
    let {
        data
    }: {
        data: { book_profile_store: BookProfileStore }
    } = $props()

    
    const store = $derived(data.book_profile_store)

    PageSlots.use({ 
        sheets, 
    })


    async function startRead()
    {
        const result = await store.handleReadData(true)

        if (result)
        {
            store.is_start_read_sheet_open = false
        }
    }
</script>


{#if store.has_reads}
    <read-page-wrapper>
        {#each store.read_previews as read, index (read.id)}
            <ReadRecordPreview 
                id = { read.id }
                index = { index }
                format = { read.format_definition }
                rendered_note = { read.rendered_note }
                start_date = { read.start_date }
                end_date = { read.end_date }
                is_in_progress = { read.is_in_progress }
                is_historical = { read.is_historical }
                onFinishRead = { read.is_in_progress ? () => store.finishRead(read.id) : undefined }
            />
        {/each}
    </read-page-wrapper>
{:else}
    <no-read-wrapper>
        <CalendarX 
            width  = { 80 }
            height = { 80 }
        />

        <p class = "empty-message">No read records for this book.</p>

        <Button
            onclick = { () => store.is_start_read_sheet_open = true }
        >
            Add the first read
        </Button>
    </no-read-wrapper>
{/if}

{#snippet sheets()}
    <StartReadSheet 
        bind:is_sheet_opened = { 
            () => store.is_start_read_sheet_open,
            (value: boolean) => { store.is_start_read_sheet_open = value; store.handleReadData(false) } }
        bind:start_date = { store.start_date }
        bind:end_date = { store.end_date }
        bind:is_dnf = { store.is_dnf }
        bind:is_historical = { store.is_historical }
        bind:edition_id = { store.edition_id }
        bind:dont_have = { store.dont_have_edition }
        editions = { store.pickable_editions }
        onFinish = { startRead }
    />
{/snippet}


<style>
    no-read-wrapper
    {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 16px;
        /* height: 100%; */
        flex: 1;
        color: var(--text-brand-secondary);

        & > :global(svg *)
        {
            stroke-width: 1;
        }

        & > :global(button)
        {
            margin-top: 16px;
        }

        p.empty-message
        {
            color: var(--text);
            text-align: center;
        }
    }
</style>