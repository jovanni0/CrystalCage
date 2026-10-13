<script lang="ts">
    import type { Size, ZoomTransform }   from "@v2/lib/rules/zoom-rules"
    import      { fullsize_cover_cache,
                  thumbnail_cover_cache } from "@root/CompositionRoot"
    import      { ZoomRules }             from "@v2/lib/rules/zoom-rules"
    import      ImageLoadingError         from "../icons/image-loading-error.svelte"
    import      Spinner                   from "../_other/spinner.svelte"



    let {
        cover_id,
        alt,
        offset,
        transform,
        container,
        is_animating,
        onNaturalSize,
    }: {
        cover_id:      string
        alt:           string
        offset:        number
        transform:     ZoomTransform
        container:     Size
        is_animating:  boolean
        onNaturalSize: (cover_id: string, size: Size) => void
    } = $props()


    let src:          string | null = $state(null)
    let has_failed:   boolean       = $state(false)
    let natural_size: Size          = $state.raw({ width: 0, height: 0 })

    const fitted_size:      Size    = $derived(ZoomRules.fittedSize(natural_size, container))
    const has_natural_size: boolean = $derived(natural_size.width > 0)


    $effect(() =>
    {
        return loadImage(cover_id)
    })


    /*
    * Shows the cached thumbnail at once (if any), then swaps in the original. Returns the cleanup that revokes the object url.
    */
    function loadImage(current_id: string): () => void
    {
        let object_url:  string | null = null
        let is_canceled: boolean       = false


        function show(blob: Blob): void
        {
            const previous_url = object_url

            object_url = URL.createObjectURL(blob)
            src        = object_url

            if (previous_url !== null)
            {
                URL.revokeObjectURL(previous_url)
            }
        }


        async function loadOriginal(): Promise<void>
        {
            const result = await fullsize_cover_cache.load(current_id)

            if (is_canceled)
            {
                return
            }

            if (!result.ok)
            {
                has_failed = object_url === null
                return
            }

            show(result.value)
        }

        src        = null
        has_failed = false

        const thumbnail = thumbnail_cover_cache.peek(current_id)

        if (thumbnail !== null)
        {
            show(thumbnail)
        }

        void loadOriginal()

        return () =>
        {
            is_canceled = true

            if (object_url !== null)
            {
                URL.revokeObjectURL(object_url)
            }
        }
    }


    function onImageLoad(event: Event): void
    {
        const image = event.currentTarget as HTMLImageElement

        natural_size = { width: image.naturalWidth, height: image.naturalHeight }
        onNaturalSize(cover_id, natural_size)
    }
</script>



<viewer-slide style:left = "{offset * 100}%">
    {#if src !== null}
        <img
            src             = { src }
            alt             = { alt }
            draggable       = "false"
            decoding        = "async"
            class:animating = { is_animating }
            style:width     = { has_natural_size ? `${fitted_size.width}px` : null }
            style:height    = { has_natural_size ? `${fitted_size.height}px` : null }
            style:transform = "translate({transform.x}px, {transform.y}px) scale({transform.scale})"
            onload          = { onImageLoad }
        />
    {:else if has_failed}
        <slide-message>
            <ImageLoadingError />
            <p>Could not load this cover</p>
        </slide-message>
    {:else}
        <Spinner />
    {/if}
</viewer-slide>



<style>
    viewer-slide
    {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 100%;

        display: flex;
        justify-content: center;
        align-items: center;
    }

    img
    {
        max-width: 100%;
        max-height: 100%;
        transform-origin: center;
        pointer-events: none;
        user-select: none;
        -webkit-user-drag: none;

        &.animating
        {
            transition: transform 0.2s ease-out;
        }
    }

    slide-message
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;

        color: var(--text-inverted-muted);
        font-size: var(--text-body-sm);
    }
</style>