<script lang="ts">
    import type { BookProfileStore } from "@v2/lib/stores/book-profile-store.svelte";
    import      { page } from "$app/state";
    import      { nav } from "@root/CompositionRoot";
    import      Star from "@root/icons/Star.svelte";
    import      CoverImage from "@v2/lib/components/covers/cover-image.svelte";
    import      CoverViewerOverlay from "@v2/lib/elements/cover-viewer-overlay.svelte";



    let {
        data
    }: {
        data: { book_profile_store: BookProfileStore }
    } = $props()

    
    const store = $derived(data.book_profile_store)
</script>



<page-wrapper>
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
            onClose           = { () => nav.closeOverlay() }
            onSetDefault      = { (cover_id) => store.setDefaultCover(cover_id) }
        />
    {/if}
</page-wrapper>


<style>
    page-wrapper
    {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
    }

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
</style>