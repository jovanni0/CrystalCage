<script lang="ts">
    import { untrack, type Snippet } from "svelte"

    type SnapPoint = {
        name:   string
        offset: number  // 0 = fully extended, 1 = closed
    }

    let {
        is_open     = $bindable(false),
        children,
        header,
        footer,
        lock_to_fit = false,
    }: {
        is_open?:     boolean
        children?:    Snippet   // scrolling content
        header?:      Snippet   // fixed above the scrolling content (e.g. a search field)
        footer?:      Snippet   // fixed below the scrolling content (e.g. the action button)
        lock_to_fit?: boolean
    } = $props()


    /* ------------------------------------------------------------------ */
    /*  constants                                                          */
    /* ------------------------------------------------------------------ */

    const MAX_HEIGHT_RATIO = 0.9   // the sheet never grows beyond 90% of the scrim
    const PADDING_TOP      = 36
    const PADDING_BOTTOM   = 24

    /* minimum speed in px/ms that counts as a fling. */
    const FLING_THRESHOLD = 0.5

    /* the pointer has to travel this far (px) before a press turns into a sheet drag.
       below it, the press is a normal click and passes through untouched. */
    const DRAG_THRESHOLD = 8

    /* how far from the vertical axis (degrees) the initial movement may point
       and still count as a sheet drag. 45 = a 90 degree cone up and down. */
    const CONE_HALF_ANGLE = 45
    const CONE_TAN        = Math.tan(CONE_HALF_ANGLE * Math.PI / 180)

    /* text-entry elements keep their own drag behaviour (selection, caret). */
    const NO_DRAG_SELECTOR =
        "textarea, select, input:not([type=radio]):not([type=checkbox]):not([type=button]):not([type=submit])"


    /* ------------------------------------------------------------------ */
    /*  measurements                                                       */
    /* ------------------------------------------------------------------ */

    let scrim_height:   number = $state(0)
    let header_height:  number = $state(0)
    let footer_height:  number = $state(0)
    let content_height: number = $state(0)   // natural height of the scrolling content
    let body_height:    number = $state(0)   // currently visible height of the scroll area

    const max_height = $derived(scrim_height * MAX_HEIGHT_RATIO)

    /* the height at which everything fits without scrolling. */
    const fit_height = $derived(
        PADDING_TOP + header_height + content_height + footer_height + PADDING_BOTTOM
    )

    const body_overflows = $derived(content_height > body_height + 1)


    /* ------------------------------------------------------------------ */
    /*  snap points                                                        */
    /* ------------------------------------------------------------------ */

    const auto_offset = $derived.by( () =>
    {
        if (!max_height || !content_height)
        {
            return 0.5
        }

        return 1 - Math.min(1, fit_height / max_height)
    })

    const snap_options: SnapPoint[] = $derived(
        lock_to_fit
        ? [
            { name: "auto",   offset: auto_offset },
            { name: "closed", offset: 1           },
        ]
        : [
            { name: "full",   offset: 0   },
            { name: "half",   offset: 0.5 },
            { name: "closed", offset: 1   },
        ]
    )

    function offsetToHeight(offset: number)
    {
        return max_height * (1 - offset)
    }

    /* sorted by height, ascending. the closed point (height 0) is always first. */
    const snap_points = $derived(
        snap_options
            .map( it => ({ ...it, height: offsetToHeight(it.offset) }) )
            .toSorted( (a, b) => a.height - b.height )
    )

    const open_points      = $derived(snap_points.filter( it => it.name !== "closed" ))
    const min_open_height  = $derived(open_points[0]?.height ?? 0)
    const max_open_height  = $derived(open_points[open_points.length - 1]?.height ?? 0)

    const snap_closed  = $derived(snap_options.find( it => it.name === "closed" )!)
    const snap_default = $derived(snap_options.find( it => it.name === "half" ) ?? snap_options[0])


    /* ------------------------------------------------------------------ */
    /*  state                                                              */
    /* ------------------------------------------------------------------ */

    let current_name: string = $state("closed")
    const current_snap = $derived(
        snap_options.find( it => it.name === current_name ) ?? snap_closed
    )

    /* the height the sheet had when it was closed, so it slides away without resizing. */
    let rest_height: number | null = $state(null)

    /* raw finger-driven height while dragging. can go below the smallest open snap. */
    let drag_raw: number | null = $state(null)

    const drag_height = $derived(
        drag_raw === null
            ? null
            : Math.min(max_open_height, Math.max(min_open_height, drag_raw))
    )

    /* how far the sheet is pulled below its smallest open height (translateY, in px). */
    const drag_overshoot = $derived(
        drag_raw === null ? 0 : Math.max(0, min_open_height - drag_raw)
    )

    /* the single source of truth for the sheet's height. */
    const visible_height = $derived.by( () =>
    {
        if (drag_height !== null)
        {
            return drag_height
        }

        if (current_name === "closed")
        {
            return rest_height ?? offsetToHeight(snap_default.offset)
        }

        return offsetToHeight(current_snap.offset)
    })


    /* ------------------------------------------------------------------ */
    /*  dragging                                                           */
    /* ------------------------------------------------------------------ */

    let sheet: HTMLElement

    type Gesture = "idle" | "pending" | "dragging" | "rejected"

    let gesture:        Gesture       = "idle"
    let pointer_id:     number | null = null
    let start_pointer_x = 0
    let start_pointer_y = 0
    let start_height    = 0
    let suppress_click  = false
    let history: { time: number, y: number }[] = []

    function onPointerDown(event: PointerEvent)
    {
        const target = event.target as HTMLElement

        if (target.closest(NO_DRAG_SELECTOR))
        {
            return
        }

        /* a scrollable list scrolls natively; dragging starts from everywhere else. */
        if (body_overflows && target.closest(".body"))
        {
            return
        }

        /* nothing is captured or moved yet. this is only a candidate for a drag. */
        gesture         = "pending"
        pointer_id      = event.pointerId
        start_pointer_x = event.clientX
        start_pointer_y = event.clientY
    }


    function beginDrag(event: PointerEvent)
    {
        gesture = "dragging"

        sheet.setPointerCapture(event.pointerId)

        /* re-base on the current position so the sheet doesn't jump by the threshold. */
        start_pointer_y = event.clientY
        start_height    = visible_height
        drag_raw        = start_height

        history = [{
            time: performance.now(),
            y:    event.clientY
        }]
    }


    function onPointerMove(event: PointerEvent)
    {
        if (event.pointerId !== pointer_id)
        {
            return
        }

        if (gesture === "pending")
        {
            const dx = event.clientX - start_pointer_x
            const dy = event.clientY - start_pointer_y

            if (Math.hypot(dx, dy) < DRAG_THRESHOLD)
            {
                return
            }

            /* decided once, when the threshold is crossed. */
            if (Math.abs(dx) > Math.abs(dy) * CONE_TAN)
            {
                gesture = "rejected"

                return
            }

            beginDrag(event)

            return
        }

        if (gesture !== "dragging")
        {
            return
        }

        const swipe_delta = event.clientY - start_pointer_y

        /* swiping down shrinks the sheet, so the sign is flipped. */
        drag_raw = Math.min(max_open_height, Math.max(0, start_height - swipe_delta))

        history.push({
            time: performance.now(),
            y:    event.clientY
        })

        const history_cutoff_time = performance.now() - 100

        history = history.filter( it => it.time >= history_cutoff_time )
    }


    function onPointerUp(event: PointerEvent)
    {
        if (event.pointerId !== pointer_id)
        {
            return
        }

        pointer_id = null

        /* it never became a drag: either a plain click or a swipe we ignored. */
        if (gesture !== "dragging")
        {
            gesture = "idle"

            return
        }

        gesture = "idle"

        /* swallow the click the browser may fire after a drag. */
        suppress_click = true
        setTimeout( () => suppress_click = false, 0 )

        const release_height = drag_raw ?? visible_height

        /* px/ms, positive = moving down. */
        let velocity = 0

        if (history.length >= 2)
        {
            const first_record       = history[0]
            const last_record        = history[history.length - 1]
            const history_time_delta = last_record.time - first_record.time

            if (history_time_delta > 0)
            {
                velocity = (last_record.y - first_record.y) / history_time_delta
            }
        }

        let target_snap: SnapPoint

        /* fling: go to the next snap point in the fling direction */
        if (Math.abs(velocity) > FLING_THRESHOLD)
        {
            if (velocity > 0)
            {
                const lower = snap_points.filter( it => it.height < release_height - 1 )

                target_snap = lower[lower.length - 1] ?? snap_points[0]
            }
            else
            {
                const higher = snap_points.filter( it => it.height > release_height + 1 )

                target_snap = higher[0] ?? snap_points[snap_points.length - 1]
            }
        }
        /* otherwise: go to the closest snap point */
        else
        {
            target_snap = snap_points.reduce( (closest, it) =>
            {
                const delta         = Math.abs(it.height      - release_height)
                const closest_delta = Math.abs(closest.height - release_height)

                return delta < closest_delta ? it : closest
            })
        }

        setSnap(target_snap)
    }


    function onKeydown(event: KeyboardEvent)
    {
        if (event.key === "Escape")
        {
            setSnap(snap_closed)
        }
    }


    function setSnap(snap_point: SnapPoint, notify_external: boolean = true)
    {
        if (snap_point.name === "closed")
        {
            /* remember where we were, so the sheet slides out at that height. */
            rest_height = visible_height
        }

        drag_raw     = null
        current_name = snap_point.name

        if (notify_external)
        {
            is_open = snap_point.name !== "closed"
        }
    }


    /* ------------------------------------------------------------------ */
    /*  effects                                                            */
    /* ------------------------------------------------------------------ */

    $effect( () =>
    {
        if (is_open)
        {
            window.addEventListener("keydown", onKeydown)

            return () => window.removeEventListener("keydown", onKeydown)
        }
    })


    /* sync `is_open` with internal snap point changes. external changes have control. */
    $effect( () =>
    {
        const open = is_open

        untrack( () =>
        {
            if (open && current_name === "closed")
            {
                setSnap(snap_default, false)
            }
            else if (!open && current_name !== "closed")
            {
                setSnap(snap_closed, false)
            }
        })
    })
</script>




<scrim
    role                = "presentation"
    bind:clientHeight   = { scrim_height }
    class:opened        = { is_open }
    class:closed        = { !is_open }
    onclick             = { () => setSnap(snap_closed) }
>
    <bottom-sheet
        bind:this       = { sheet }
        role            = "presentation"
        style:height    = "{visible_height}px"
        style:transform = { drag_raw !== null ? `translateY(${drag_overshoot}px)` : undefined }
        class:opened    = { is_open }
        class:closed    = { !is_open }
        class:dragging  = { drag_raw !== null }
        onclickcapture  = { (event: MouseEvent) =>
        {
            if (suppress_click)
            {
                suppress_click = false
                event.stopPropagation()
                event.preventDefault()
            }
        }}
        onclick         = { (event: MouseEvent) => event.stopPropagation() }
        onpointerdown   = { onPointerDown }
        onpointermove   = { onPointerMove }
        onpointerup     = { onPointerUp }
        onpointercancel = { onPointerUp }
    >
        <div class="thumb"></div>

        {#if header}
            <div class="header" bind:clientHeight = { header_height }>
                {@render header()}
            </div>
        {/if}

        <div
            class="body"
            class:scrollable  = { body_overflows }
            bind:clientHeight = { body_height }
        >
            <div class="content" bind:clientHeight = { content_height }>
                {@render children?.()}
            </div>
        </div>

        {#if footer}
            <div class="footer" bind:clientHeight = { footer_height }>
                {@render footer()}
            </div>
        {/if}
    </bottom-sheet>
</scrim>



<style>
    scrim
    {
        background-color: rgba(0, 0, 0, 25%);

        position: absolute;
        inset: 0;
        z-index: 999;

        opacity: 0;
        visibility: hidden;
        pointer-events: none;

        &.opened
        {
            transition:
                opacity    250ms cubic-bezier(0.05, 0.7, 0.1, 1),
                visibility 0ms   linear;

            opacity: 1;
            visibility: visible;
            pointer-events: auto;
        }

        &.closed
        {
            transition:
                opacity    200ms cubic-bezier(0.05, 0.7, 0.1, 1),
                visibility 0ms   linear 200ms;

            opacity: 0;
            visibility: hidden;
            pointer-events: none;
        }
    }

    bottom-sheet
    {
        position: absolute;
        inset: auto 0 0 0;

        display: flex;
        flex-direction: column;
        box-sizing: border-box;
        overflow: hidden;

        /* `height` is set from script. the sheet is anchored to the bottom of the
           screen, so the footer stays put while the list grows and shrinks. */
        background-color: var(--bg);
        border-radius: 32px 32px 0 0;
        padding: 36px 20px 24px 20px;

        touch-action: none;
        user-select: none;
        overscroll-behavior: contain;

        transform: translateY(100%);

        &.opened
        {
            transition:
                height    350ms cubic-bezier(0.05, 0.7, 0.1, 1),
                transform 250ms cubic-bezier(0.05, 0.7, 0.1, 1);
            transform: translateY(0);
        }

        &.closed
        {
            transition:
                transform 200ms cubic-bezier(0.05, 0.7, 0.1, 1);
            transform: translateY(100%);
        }

        /* no transitions while the finger is on the sheet. */
        &.dragging
        {
            transition: none;
        }
    }

    .thumb
    {
        position: absolute;
        inset: 16px 50% auto auto;
        transform: translateX(50%);

        width: 32px;
        height: 4px;
        background-color: var(--bg-inverted-muted);
        border-radius: 100px;
    }

    .header
    {
        flex: none;
        padding-bottom: 16px;
    }

    .body
    {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        overscroll-behavior: contain;

        &.scrollable
        {
            touch-action: pan-y;
        }
    }

    .content
    {
        display: flex;
        flex-direction: column;
    }

    .footer
    {
        flex: none;
        padding-top: 16px;
        display: flex;
        flex-direction: column;
    }
</style>