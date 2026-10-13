<script lang="ts">
    import type { Size }           from "@v2/lib/rules/zoom-rules"
    import      { SvelteMap }      from "svelte/reactivity"
    import      { fade }           from "svelte/transition"
    import      { ZoomRules }      from "@v2/lib/rules/zoom-rules"
    import      { ViewerGestures } from "@v2/lib/utils/viewer-gestures.svelte"
    import      CoverViewerSlide   from "@v2/lib/components/covers/cover-viewer.svelte"
    import      Ex                 from "@v2/lib/components/icons/ex.svelte"
    import      Star               from "@root/icons/Star.svelte"



    type ViewerCover =
    {
        id:         string
        label:      string | null
        added_at:   string
        resolution: string
        is_default: boolean
    }

    type VisibleSlide =
    {
        cover:  ViewerCover
        index: number
    }



    let {
        covers,
        start_index,
        is_saving_default,
        onClose,
        onSetDefault,
    }: {
        covers:            ViewerCover[]
        start_index:       number
        is_saving_default: boolean
        onClose:           () => void
        onSetDefault:      (cover_id: string) => void
    } = $props()


    // svelte-ignore state_referenced_locally
    let selected_index:    number  = $state(start_index)
    let is_chrome_visible: boolean = $state(true)
    let container_width:   number  = $state(0)
    let container_height:  number  = $state(0)

    const natural_sizes = new SvelteMap<string, Size>()

    const current_index:  number         = $derived(Math.min(selected_index, covers.length - 1))
    const current_cover:  ViewerCover    = $derived(covers[current_index])
    const container:      Size           = $derived({ width: container_width, height: container_height })
    const fitted_size:    Size           = $derived(ZoomRules.fittedSize(natural_sizes.get(current_cover.id) ?? { width: 0, height: 0 }, container))
    const visible_slides: VisibleSlide[] = $derived(buildVisibleSlides())

    const gestures = new ViewerGestures({
        getContainer:  () => container,
        getFittedSize: () => fitted_size,
        canPage:       (direction) => isInRange(current_index + direction),
        onPage:        (direction) => selected_index = current_index + direction,
        onDismiss:     () => onClose(),
        onTap:         () => is_chrome_visible = !is_chrome_visible,
    })

    const backdrop_opacity: number = $derived(1 - Math.min(gestures.dismiss_offset_y / 400, 0.6))


    function isInRange(index: number): boolean
    {
        return index >= 0 && index < covers.length
    }


    function buildVisibleSlides(): VisibleSlide[]
    {
        const slides: VisibleSlide[] = []

        for (let index = current_index - 1; index <= current_index + 1; index++)
        {
            if (isInRange(index))
            {
                slides.push({ cover: covers[index], index: index })
            }
        }

        return slides
    }


    function onKeyDown(event: KeyboardEvent): void
    {
        if (event.key === "Escape")
        {
            onClose()
            return
        }

        if (event.key === "ArrowRight")
        {
            gestures.page(1)
            return
        }

        if (event.key === "ArrowLeft")
        {
            gestures.page(-1)
        }
    }
</script>



<svelte:window onkeydown = { onKeyDown } />
<cover-viewer-overlay
    role                     = "dialog"
    aria-modal               = "true"
    aria-label               = "Cover viewer"
    style:--backdrop-opacity = { backdrop_opacity }
>
    <viewer-stage
        bind:clientWidth  = { container_width }
        bind:clientHeight = { container_height }
        {@attach gestures.attach}
    >
        <viewer-strip
            class:settling  = { gestures.is_settling }
            style:transform = "translate(calc({-current_index * 100}% + {gestures.page_offset_x}px), {gestures.dismiss_offset_y}px)"
        >
            {#each visible_slides as slide (slide.cover.id)}
                <CoverViewerSlide
                    cover_id      = { slide.cover.id }
                    alt           = { slide.cover.label ?? "" }
                    index         = { slide.index }
                    transform     = { slide.index === current_index ? gestures.transform : ZoomRules.IDENTITY }
                    container     = { container }
                    is_animating  = { gestures.is_settling }
                    onNaturalSize = { (cover_id, size) => natural_sizes.set(cover_id, size) }
                />
            {/each}
        </viewer-strip>
    </viewer-stage>

    {#if is_chrome_visible}
        <viewer-topbar transition:fade = {{ duration: 150 }}>
            <button
                class      = "icon"
                aria-label = "Close"
                onclick    = { onClose }
                {@attach (node) => node.focus()}
            >
                <Ex />
            </button>

            <p class = "label">{ current_cover.label ?? "" }</p>

            <button
                class         = "icon"
                class:default = { current_cover.is_default }
                aria-label    = "Main cover"
                aria-pressed  = { current_cover.is_default }
                disabled      = { is_saving_default }
                onclick       = { () => onSetDefault(current_cover.id) }
            >
                <Star />
            </button>
        </viewer-topbar>

        <viewer-bottombar transition:fade = {{ duration: 150 }}>
            <p class = "counter">{ current_index + 1 } of { covers.length }</p>

            <dl>
                <dt>Added on:</dt>
                <dd>{ current_cover.added_at }</dd>

                <dt>Size:</dt>
                <dd>{ current_cover.resolution }</dd>
            </dl>
        </viewer-bottombar>
    {/if}
</cover-viewer-overlay>



<style>
    cover-viewer-overlay
    {
        position: fixed;
        inset: 0;
        z-index: 100;
        display: block;

        background-color: rgba(0, 0, 0, var(--backdrop-opacity));
        color: var(--text-inverted-muted);
    }

    viewer-stage
    {
        position: absolute;
        inset: 0;
        display: block;
        overflow: hidden;
    }

    viewer-strip
    {
        position: absolute;
        inset: 0;
        display: block;
        will-change: transform;

        &.settling
        {
            transition: transform 0.25s ease-out;
        }
    }

    viewer-topbar,
    viewer-bottombar
    {
        position: absolute;
        left: 0;
        right: 0;

        display: flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;

        background-color: rgba(0, 0, 0, 60%);
    }

    viewer-topbar
    {
        top: 0;
        padding-top: calc(8px + env(safe-area-inset-top));
    }

    viewer-bottombar
    {
        bottom: 0;
        justify-content: space-between;
        padding-bottom: calc(8px + env(safe-area-inset-bottom));
        font-size: var(--text-caption);
    }

    p.label
    {
        flex: 1;
        text-align: center;
        font-size: var(--text-body);

        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 4;
        line-clamp: 4;
        overflow: hidden;
    }

    button.icon
    {
        flex-shrink: 0;
        width: 48px;
        height: 48px;

        display: flex;
        justify-content: center;
        align-items: center;

        background: transparent;
        color: inherit;

        &.default > :global(svg)
        {
            fill: currentColor;
        }

        &:disabled
        {
            opacity: 0.5;
        }
    }

    dl
    {
        display: grid;
        grid-template-columns: auto auto;
        column-gap: 8px;
        margin: 0;
    }

    dt
    {
        text-align: right;
    }

    dd
    {
        margin: 0;
    }

    @media (prefers-reduced-motion: reduce)
    {
        viewer-strip.settling
        {
            transition: none;
        }
    }
</style>