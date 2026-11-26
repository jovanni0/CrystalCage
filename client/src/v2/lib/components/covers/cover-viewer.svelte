<script lang="ts">
    import type { Size, 
                  ZoomTransform }       from "@v2/lib/rules/zoom-rules"
    import type { CoverFrameImageSize } from "./cover-frame.svelte";
    import      { ZoomRules }           from "@v2/lib/rules/zoom-rules"
    import      CoverImage              from "./cover-image.svelte";



    let {
        cover_id,
        alt,
        index,
        transform,
        container,
        is_animating,
        onNaturalSize,
    }: {
        cover_id:      string
        alt:           string
        index:        number
        transform:     ZoomTransform
        container:     Size
        is_animating:  boolean
        onNaturalSize: (cover_id: string, size: Size) => void
    } = $props()


    let natural_size: Size = $state.raw({ width: 0, height: 0 })

    const fitted_size:      Size    = $derived(ZoomRules.fittedSize(natural_size, container))
    const has_natural_size: boolean = $derived(natural_size.width > 0)


    function rememeberSize(size: CoverFrameImageSize): void
    {
        natural_size = size
        onNaturalSize(cover_id, size)
    }
</script>



<viewer-slide 
    style:left = "{index * 100}%"
>
    <zoom-layer
        class:animating = { is_animating }
        style:width     = { has_natural_size ? `${fitted_size.width}px` : null }
        style:height    = { has_natural_size ? `${fitted_size.height}px` : null }
        style:transform = "translate({transform.x}px, {transform.y}px) scale({transform.scale})"
    >
        <CoverImage 
            cover_id                 = { cover_id }
            cover_type               = "original"
            alt                      = { alt }
            fit                      = "contain"
            is_lazy                  = { false }
            should_preview_thumbnail = { true }
            onImageLoad              = { rememeberSize }
        />
    </zoom-layer>
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

        will-change: transform;
        contain: layout paint;
    }

    zoom-layer
    {
        display: block;
        transform-origin: center;
        pointer-events: none;
        user-select: none;

        &.animating
        {
            transition: transform 0.2s ease-out;
        }
    }
</style>