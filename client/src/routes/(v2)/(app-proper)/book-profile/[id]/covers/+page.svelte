<script lang="ts">
    import type { BookProfileStore } from "@v2/lib/stores/book-profile-store.svelte";
    import      { page } from "$app/state";
    import      { nav } from "@root/CompositionRoot";
    import      Star from "@root/icons/Star.svelte";
    import      CoverImage from "@v2/lib/components/covers/cover-image.svelte";
    import      CoverViewerOverlay from "@v2/lib/elements/cover-viewer-overlay.svelte";
    import Button from "@v2/lib/components/buttons/button.svelte";
    import FileImage from "@v2/lib/components/icons/file-image.svelte";
    import { PageSlots } from "@v2/lib/utils/page-slots.svelte";
    import ManageCover from "@v2/lib/elements/manage-cover.svelte";



    let {
        data
    }: {
        data: { book_profile_store: BookProfileStore }
    } = $props()

    PageSlots.use({ sheets, })

    
    const store = $derived(data.book_profile_store)
</script>



{#if store.has_covers}
    <cover-grid>
        {#each store.covers_preview as cover, index (cover.id)}
            <cover-wrapper>
                <content-wrapper>
                    <cover-image-wrapper>
                        <CoverImage 
                            cover_id   = { cover.id }
                            cover_type = "thumbnail"
                            alt        = { cover.label ?? undefined }
                        />
                    </cover-image-wrapper>
        
                    <button
                        class      = "view"
                        aria-label = { `View cover ${index + 1}${cover.label ? `, ${cover.label}` : ""}` }
                        onclick    = { () => nav.openOverlay({ cover_viewer_index: index }) }
                    ></button>
        
                    <button 
                        class         = "star"
                        class:default = { cover.is_default }
                        onclick       = { () => store.setDefaultCover(cover.id) }
                    >
                        <Star />
                    </button>
        
                    <p class = "resolution">{ cover.resolution }</p>
                </content-wrapper>

                <p class = "label">{ cover.label }</p>
            </cover-wrapper>
        {/each}

        {#if page.state.cover_viewer_index !== undefined && store.covers_preview.length > 0}
            <CoverViewerOverlay
                covers            = { store.covers_preview }
                start_index       = { page.state.cover_viewer_index }
                is_saving_default = { store.is_saving_cover }
                onEdit            = { (id: string) => store.triggerCoverEditSheet(id) }
                onClose           = { () => nav.closeOverlay() }
                onSetDefault      = { (cover_id) => store.setDefaultCover(cover_id) }
            />
        {/if}
    </cover-grid>
{:else}
    <no-covers-wrapper>
        <FileImage 
            width  = { 80 }
            height = { 80 }
        />

        <p class = "empty-message">No covers added yet.<br/>Add one from your files or a link.</p>

        <Button
            onclick = { () => store.is_manage_cover_sheet_open = true }
        >
            Add the first cover
        </Button>
    </no-covers-wrapper>
{/if}

{#snippet sheets()}
    <ManageCover 
        bind:is_open           = { store.is_manage_cover_sheet_open }
        bind:label             = { store.manage_cover_sheet_label }
        error_message          = { store.manage_cover_sheet_error }
        cover_id               = { store.manage_cover_sheet_cover_id }
        cover_file             = { store.manage_cover_sheet_file }
        resolution             = { store.manage_cover_sheet_resolution }
        format                 = { store.manage_cover_sheet_format }
        is_source_enabled      = { store.is_source_enabled }
        is_main_button_enabled = { store.is_manage_cover_button_enabled }
        onFetch                = { async (url: string) => await store.addFromUrl(url) }
        onUpload               = { (file: File) => store.addFromDevice(file) }
        onFinish               = { async () => await store.finishManageCover() }
        onClear                = { () => store.clearSheetData() }
        onDelete               = { (id: string) => store.deleteCover(id) }
    />
{/snippet}


<style>
    no-covers-wrapper
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

    cover-grid
    {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        cover-wrapper
        {
            display: flex;
            flex-direction: column;
            width: 100%;
    
            cover-image-wrapper
            {
                display: block;
                width: 100%;
                aspect-ratio: 2/3;
                overflow: hidden;
    
                border: 1px solid var(--text-placeholder);
                border-radius: 8px;
                box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.2);
            }
            
            p.label
            {
                text-align: center;
                padding: 2px 0;
                font-size: var(--text-caption);
                display: block;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
    
            content-wrapper
            {
                width: 100%;
                position: relative;
                aspect-ratio: 2/3;
                border-radius: 8px;
                overflow: hidden;
    
                button.star
                {
                    position: absolute;
                    inset: 0 0 auto auto;
                    width: 48px;
                    height: 48px;
        
                    display: flex;
                    justify-content: center;
                    align-items: center;
        
                    border-bottom-left-radius: 8px;
        
                    & > :global(svg)
                    {
                        color: var(--text-inverted-muted);
                    }
        
                    &.default
                    {
                        background-color: rgba(0, 0, 0, 75%);
                        color: var(--text-inverted-muted);
        
                        & > :global(svg)
                        {
                            fill: currentColor;
                        }
                    }
                }
        
                button.view
                {
                    position: absolute;
                    inset: 0;
        
                    &:focus-visible
                    {
                        outline: 2px solid var(--text);
                        outline-offset: 2px;
                    }
                }
        
                p.resolution
                {
                    position: absolute;
                    inset: auto 0 0 0;
        
                    background-color: rgba(0, 0, 0, 75%);
                    color: var(--text-inverted-muted);
        
                    text-align: center;
                    padding: 2px 0;
                    font-size: var(--text-caption);
                }
            }
    
        }
    }
</style>