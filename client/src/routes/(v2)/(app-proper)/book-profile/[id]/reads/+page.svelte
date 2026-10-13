<script lang="ts">
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
{#snippet sheets()}
    <StartReadSheet 
        bind:is_sheet_opened = { 
            () => store.is_start_read_sheet_open,
            (value: boolean) => { store.is_start_read_sheet_open = value; store.handleReadData(false); console.log("closed"); } }
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