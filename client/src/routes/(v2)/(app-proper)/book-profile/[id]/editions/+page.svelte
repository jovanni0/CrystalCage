<script lang="ts">
    import type { BookProfileStore } from "@v2/lib/stores/book-profile-store.svelte";
    import      { nav } from "@root/CompositionRoot";
    import      Button from "@v2/lib/components/buttons/button.svelte";
    import      FileType from "@v2/lib/components/icons/file-type.svelte";
    import      EditionPreviewListItem from "@v2/lib/components/list-items/edition-preview-list-item.svelte";
    import      EditionDetailsSheet from "@v2/lib/elements/edition-details-sheet.svelte";
    import { PageSlots } from "@v2/lib/utils/page-slots.svelte";


    
    let {
        data
    }: {
        data: { book_profile_store: BookProfileStore }
    } = $props()

    
    const store = $derived(data.book_profile_store)

    PageSlots.use({ sheets })


    function triggerSheet(id: string)
    {
        store.inspected_edition_id = id
        store.is_edition_details_sheet_open = true
    }

    async function deleteInspectedEdition()
    {
        const success = await store.deleteInspectedEdition()

        if (success)
        {
            store.is_edition_details_sheet_open = false
            return
        }
    }
</script>


{#if store.has_editions}
    <edition-list>
        {#each store.edition_preview as edition (edition.id)}
            <EditionPreviewListItem 
                id           = { edition.id }
                format       = { edition.format }
                label        = { edition.label }
                read_status  = { edition.read_status }
                release_date = { edition.release_date }
                onClick      = { () => triggerSheet(edition.id) }
            />
        {/each}
    </edition-list>
{:else}
    <no-editions-wrapper>
        <FileType 
            width  = { 80 }
            height = { 80 }
        />

        <p class = "empty-message">No editions added yet.<br/>Editions are versions of the book.</p>

        <Button
            onclick = { () => nav.goTo(`/manage-editions/${store.book_id}`)}
        >
            Add the first edition
        </Button>
    </no-editions-wrapper>
{/if}
{#snippet sheets()}
    <EditionDetailsSheet 
        bind:is_open    = { store.is_edition_details_sheet_open }
        label           = { store.inspected_edition_details.label }
        format          = { store.inspected_edition_details.format }
        release_date    = { store.inspected_edition_details.release_date ?? "" }
        read_status     = { store.inspected_edition_details.read_status }
        file_size       = { store.inspected_edition_details.file_size }
        added_at        = { store.inspected_edition_details.added_at }
        is_downloadable = { false }
        onTrash         = { deleteInspectedEdition }
        onDownload      = { () => {} }
    />
{/snippet}



<style>
    edition-list
    {
        display: flex;
        flex-direction: column;
    }
    no-editions-wrapper
    {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 16px;
        height: 100%;
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