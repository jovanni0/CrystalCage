<script lang="ts">
    import type { Point, Size, ZoomTransform } from "@v2/lib/rules/zoom-rules"
    import      { tick }                         from "svelte"
    import      { SvelteMap }                    from "svelte/reactivity"
    import      { fade }                         from "svelte/transition"
    import      { DOUBLE_TAP_ZOOM_SCALE,
                  ZoomRules }                    from "@v2/lib/rules/zoom-rules"
    import      CoverViewerSlide                 from "@v2/lib/components/covers/cover-viewer-slide.svelte"
    import      Ex                               from "@v2/lib/components/icons/ex.svelte"
    import Star from "@root/icons/Star.svelte";



    type Gesture = "none" | "undecided" | "pan" | "pinch" | "page" | "dismiss"

    type VisibleSlide =
    {
        cover:  ViewerCover
        offset: number
    }

    type ViewerCover =
    {
        id:         string
        label:      string | null
        added_at:   string
        resolution: string
        is_default: boolean
    }


    const GESTURE_DECISION_DISTANCE = 10
    const DOUBLE_TAP_DELAY_MS       = 250
    const DOUBLE_TAP_DISTANCE       = 30
    const PAGE_COMMIT_RATIO         = 0.25
    const DISMISS_COMMIT_DISTANCE   = 120
    const FLICK_VELOCITY            = 0.5      // px per ms
    const EDGE_RESISTANCE           = 3



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


    // only the starting value is taken from the prop; swiping changes it from here on
    let selected_index:    number        = $state(start_index)
    let is_chrome_visible: boolean       = $state(true)
    let is_settling:       boolean       = $state(false)
    let transform:         ZoomTransform = $state.raw(ZoomRules.IDENTITY)
    let page_offset_x:     number        = $state(0)
    let dismiss_offset_y:  number        = $state(0)
    let container_width:   number        = $state(0)
    let container_height:  number        = $state(0)

    const natural_sizes = new SvelteMap<string, Size>()

    const current_index:    number         = $derived(Math.min(selected_index, covers.length - 1))
    const current_cover:    ViewerCover    = $derived(covers[current_index])
    const container:        Size           = $derived({ width: container_width, height: container_height })
    const fitted_size:      Size           = $derived(ZoomRules.fittedSize(natural_sizes.get(current_cover.id) ?? { width: 0, height: 0 }, container))
    const visible_slides:   VisibleSlide[] = $derived(buildVisibleSlides())
    const backdrop_opacity: number         = $derived(1 - Math.min(dismiss_offset_y / 400, 0.6))


    // gesture bookkeeping, read only by the handlers, so not reactive
    const pointers = new Map<number, Point>()

    let gesture:         Gesture        = "none"
    let stage_rect:      DOMRect | null = null
    let start_point:     Point          = { x: 0, y: 0 }
    let start_time:      number         = 0
    let start_transform: ZoomTransform  = ZoomRules.IDENTITY
    let pinch_distance:  number         = 1
    let pinch_midpoint:  Point          = { x: 0, y: 0 }
    let last_tap:        { point: Point, time: number } | null = null
    let tap_timer:       ReturnType<typeof setTimeout> | null  = null


    $effect(() =>
    {
        return () => cancelPendingTap()
    })


    function buildVisibleSlides(): VisibleSlide[]
    {
        const slides: VisibleSlide[] = []

        for (let offset = -1; offset <= 1; offset++)
        {
            const cover = covers[current_index + offset]

            if (cover !== undefined)
            {
                slides.push({ cover: cover, offset: offset })
            }
        }

        return slides
    }


    function pointOf(event: PointerEvent): Point
    {
        const rect = stage_rect ?? (event.currentTarget as HTMLElement).getBoundingClientRect()

        return {
            x: event.clientX - rect.left - rect.width / 2,
            y: event.clientY - rect.top - rect.height / 2,
        }
    }


    function onPointerDown(event: PointerEvent): void
    {
        const stage = event.currentTarget as HTMLElement

        stage.setPointerCapture(event.pointerId)
        stage_rect  = stage.getBoundingClientRect()
        is_settling = false
        pointers.set(event.pointerId, pointOf(event))

        if (pointers.size === 1)
        {
            startSinglePointer(pointOf(event), true)
            return
        }

        if (pointers.size === 2)
        {
            startPinch()
        }
    }


    function startSinglePointer(point: Point, can_be_tap: boolean): void
    {
        start_point     = point
        start_time      = performance.now()
        start_transform = transform

        if (transform.scale > 1)
        {
            gesture = "pan"
            return
        }

        gesture = can_be_tap ? "undecided" : "none"
    }


    function startPinch(): void
    {
        const [ first, second ] = [ ...pointers.values() ]

        page_offset_x    = 0
        dismiss_offset_y = 0
        start_transform  = transform
        pinch_distance   = Math.max(1, ZoomRules.distance(first, second))
        pinch_midpoint   = ZoomRules.midpoint(first, second)
        gesture          = "pinch"
    }


    function onPointerMove(event: PointerEvent): void
    {
        if (!pointers.has(event.pointerId))
        {
            return
        }

        const point = pointOf(event)
        const delta = { x: point.x - start_point.x, y: point.y - start_point.y }

        pointers.set(event.pointerId, point)

        if (gesture === "undecided")
        {
            gesture = decideGesture(delta)
        }

        applyGesture(delta)
    }


    function decideGesture(delta: Point): Gesture
    {
        if (Math.hypot(delta.x, delta.y) < GESTURE_DECISION_DISTANCE)
        {
            return "undecided"
        }

        if (Math.abs(delta.x) > Math.abs(delta.y))
        {
            return "page"
        }

        if (delta.y > 0)
        {
            return "dismiss"
        }

        return "none"
    }


    function applyGesture(delta: Point): void
    {
        if (gesture === "pinch")
        {
            updatePinch()
        }
        else if (gesture === "pan")
        {
            const panned = { scale: start_transform.scale, x: start_transform.x + delta.x, y: start_transform.y + delta.y }

            transform = ZoomRules.clampOffset(panned, fitted_size, container)
        }
        else if (gesture === "page")
        {
            page_offset_x = pageOffsetFor(delta.x)
        }
        else if (gesture === "dismiss")
        {
            dismiss_offset_y = Math.max(0, delta.y)
        }
    }


    function updatePinch(): void
    {
        const [ first, second ] = [ ...pointers.values() ]
        const midpoint = ZoomRules.midpoint(first, second)
        const scale    = start_transform.scale * ZoomRules.distance(first, second) / pinch_distance
        const zoomed   = ZoomRules.zoomAround(start_transform, scale, pinch_midpoint)

        transform = ZoomRules.clampOffset({
            scale: zoomed.scale,
            x:     zoomed.x + midpoint.x - pinch_midpoint.x,
            y:     zoomed.y + midpoint.y - pinch_midpoint.y,
        }, fitted_size, container)
    }


    /*
    * Past the first or last cover the strip moves at a third of the finger's distance, as a "nothing more here" cue.
    */
    function pageOffsetFor(delta_x: number): number
    {
        const is_past_start = delta_x > 0 && current_index === 0
        const is_past_end   = delta_x < 0 && current_index === covers.length - 1

        if (is_past_start || is_past_end)
        {
            return delta_x / EDGE_RESISTANCE
        }

        return delta_x
    }


    function onPointerUp(event: PointerEvent): void
    {
        if (!pointers.has(event.pointerId))
        {
            return
        }

        pointers.delete(event.pointerId)

        if (gesture === "pinch" && pointers.size === 1)
        {
            const [ remaining ] = [ ...pointers.values() ]

            startSinglePointer(remaining, false)
            return
        }

        if (pointers.size === 0)
        {
            finishGesture(pointOf(event))
        }
    }


    function onPointerCancel(event: PointerEvent): void
    {
        pointers.delete(event.pointerId)

        if (pointers.size > 0)
        {
            return
        }

        gesture          = "none"
        is_settling      = true
        page_offset_x    = 0
        dismiss_offset_y = 0
        settleZoom()
    }


    function finishGesture(point: Point): void
    {
        const elapsed_ms = Math.max(1, performance.now() - start_time)
        const delta      = { x: point.x - start_point.x, y: point.y - start_point.y }
        const finished   = gesture

        gesture     = "none"
        is_settling = true

        if (finished === "undecided")
        {
            handleTap(point)
        }
        else if (finished === "page")
        {
            finishPageSwipe(delta.x, delta.x / elapsed_ms)
        }
        else if (finished === "dismiss")
        {
            finishDismiss(delta.y, delta.y / elapsed_ms)
        }
        else if (finished === "pan" || finished === "pinch")
        {
            settleZoom()
        }
    }


    function settleZoom(): void
    {
        if (transform.scale < 1.05)
        {
            transform = ZoomRules.IDENTITY
        }
    }


    function finishPageSwipe(delta_x: number, velocity_x: number): void
    {
        const is_far_enough_left  = delta_x < -container_width * PAGE_COMMIT_RATIO || velocity_x < -FLICK_VELOCITY
        const is_far_enough_right = delta_x > container_width * PAGE_COMMIT_RATIO || velocity_x > FLICK_VELOCITY

        if (is_far_enough_left && current_index < covers.length - 1)
        {
            void changeCover(current_index + 1)
            return
        }

        if (is_far_enough_right && current_index > 0)
        {
            void changeCover(current_index - 1)
            return
        }

        page_offset_x = 0
    }


    /*
    * Re-bases the strip on the new cover at the same on-screen position, forces a layout so the browser commits it, then animates to rest, so the slide keeps moving instead of jumping.
    */
    async function changeCover(next_index: number): Promise<void>
    {
        const direction = next_index - current_index

        is_settling    = false
        transform      = ZoomRules.IDENTITY
        page_offset_x  = page_offset_x + direction * container_width
        selected_index = next_index

        await tick()
        stage_rect = null
        document.body.getBoundingClientRect()

        is_settling   = true
        page_offset_x = 0
    }


    function finishDismiss(delta_y: number, velocity_y: number): void
    {
        if (delta_y > DISMISS_COMMIT_DISTANCE || velocity_y > FLICK_VELOCITY)
        {
            onClose()
            return
        }

        dismiss_offset_y = 0
    }


    /*
    * A single tap waits for a possible second tap before toggling the bars, so a double tap does not also flash them.
    */
    function handleTap(point: Point): void
    {
        const now           = performance.now()
        const is_double_tap = last_tap !== null
            && now - last_tap.time < DOUBLE_TAP_DELAY_MS
            && ZoomRules.distance(point, last_tap.point) < DOUBLE_TAP_DISTANCE

        if (is_double_tap)
        {
            cancelPendingTap()
            last_tap = null
            toggleZoomAt(point)
            return
        }

        last_tap  = { point: point, time: now }
        tap_timer = setTimeout(() =>
        {
            tap_timer         = null
            is_chrome_visible = !is_chrome_visible
        }, DOUBLE_TAP_DELAY_MS)
    }


    function cancelPendingTap(): void
    {
        if (tap_timer !== null)
        {
            clearTimeout(tap_timer)
            tap_timer = null
        }
    }


    function toggleZoomAt(point: Point): void
    {
        if (transform.scale > 1)
        {
            transform = ZoomRules.IDENTITY
            return
        }

        const zoomed = ZoomRules.zoomAround(transform, DOUBLE_TAP_ZOOM_SCALE, point)

        transform = ZoomRules.clampOffset(zoomed, fitted_size, container)
    }


    function onKeyDown(event: KeyboardEvent): void
    {
        if (event.key === "Escape")
        {
            onClose()
            return
        }

        if (event.key === "ArrowRight" && current_index < covers.length - 1)
        {
            void changeCover(current_index + 1)
            return
        }

        if (event.key === "ArrowLeft" && current_index > 0)
        {
            void changeCover(current_index - 1)
        }
    }
</script>



<svelte:window onkeydown = { onKeyDown } />



<cover-viewer
    role                     = "dialog"
    aria-modal               = "true"
    aria-label               = "Cover viewer"
    style:--backdrop-opacity = { backdrop_opacity }
>
    <viewer-stage
        bind:clientWidth  = { container_width }
        bind:clientHeight = { container_height }
        onpointerdown     = { onPointerDown }
        onpointermove     = { onPointerMove }
        onpointerup       = { onPointerUp }
        onpointercancel   = { onPointerCancel }
    >
        <viewer-strip
            class:settling  = { is_settling }
            style:transform = "translate({page_offset_x}px, {dismiss_offset_y}px)"
        >
            {#each visible_slides as slide (slide.cover.id)}
                <CoverViewerSlide
                    cover_id      = { slide.cover.id }
                    alt           = { slide.cover.label ?? "" }
                    offset        = { slide.offset }
                    transform     = { slide.offset === 0 ? transform : ZoomRules.IDENTITY }
                    container     = { container }
                    is_animating  = { is_settling }
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
</cover-viewer>



<style>
    cover-viewer
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
        touch-action: none;
    }

    viewer-strip
    {
        position: absolute;
        inset: 0;
        display: block;

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
        font-size: var(--text-body-sm);

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