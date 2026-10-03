<script lang="ts">
    import { untrack, type Snippet } from "svelte"



    type SheetPolicy = "fit" | "fit-expand"
    type SnapName    = "closed" | "auto" | "full"

    let {
        is_open  = $bindable(false),
        policy   = "fit",
        children,
        header,
        footer,
        class: class_name,
    }: {
        is_open?:  boolean
        policy?:   SheetPolicy   // fit: content height, not resizable. fit-expand: content height, expandable to full.
        children?: Snippet       // scrolling content
        header?:   Snippet       // fixed above the scrolling content (e.g. a search field)
        footer?:   Snippet       // fixed below the scrolling content (e.g. the action button)
        class?:    string
    } = $props()


    /* ------------------------------------------------------------------ */
    /*  constants                                                          */
    /* ------------------------------------------------------------------ */

    const MAX_HEIGHT_RATIO = 0.9   // the sheet never grows beyond 90% of the scrim
    const PADDING_TOP      = 36
    const PADDING_BOTTOM   = 24

    /* minimum speed in px/ms that counts as a fling. */
    const FLING_THRESHOLD = 0.5

    /* how long (ms) after the last scrim resize the height animates again. */
    const SCRIM_SETTLE_TIME = 150

    /* how far back (ms) the release velocity is measured. */
    const VELOCITY_WINDOW = 100

    /* the pointer has to travel this far (px) before a press turns into a sheet drag. */
    const DRAG_THRESHOLD = 8

    /* the initial movement must stay within 45 degrees of the vertical axis to count as a drag. */
    const CONE_TAN = Math.tan(45 * Math.PI / 180)

    /* text-entry elements keep their own drag behaviour (selection, caret). */
    const TEXT_ENTRY_SELECTOR = "textarea, select"


    /* ------------------------------------------------------------------ */
    /*  measurements                                                       */
    /* ------------------------------------------------------------------ */

    let scrim_height   = $state(0)
    let header_height  = $state(0)
    let footer_height  = $state(0)
    let content_height = $state(0)

    const max_height = $derived(scrim_height * MAX_HEIGHT_RATIO)

    /* the height at which everything fits without scrolling (uncapped). */
    const natural_height = $derived(
        PADDING_TOP + header_height + content_height + footer_height + PADDING_BOTTOM
    )

    /* fit-expand never shrinks while open: it keeps the tallest content height since opening.
       reset in setSnap when the sheet opens. kept uncapped, so a shorter scrim (keyboard)
       only caps it and the sheet returns to its size once the scrim grows back. */
    let peak_height = $state(0)

    const content_fit_height = $derived(
        policy === "fit"
            ? natural_height
            : Math.max(natural_height, peak_height)
    )

    const auto_height = $derived(Math.min(content_fit_height, max_height))

    $effect( () =>
    {
        peak_height = content_fit_height
    })


    /* the scrim resizes when the keyboard opens or closes. the sheet follows it instantly:
       an animated height would overshoot and lag behind the keyboard. */
    let is_scrim_resizing = $state(false)

    $effect.pre( () =>
    {
        if (scrim_height === 0)
        {
            return
        }

        is_scrim_resizing = true

        const timer = setTimeout( () => is_scrim_resizing = false, SCRIM_SETTLE_TIME )

        return () => clearTimeout(timer)
    })

    /* ------------------------------------------------------------------ */
    /*  snap points                                                        */
    /* ------------------------------------------------------------------ */

    /* sorted by height, ascending. auto is always the smallest open point. */
    const snap_names: SnapName[] = $derived(
        policy === "fit-expand"
            ? ["closed", "auto", "full"]
            : ["closed", "auto"]
    )

    const snap_heights: Record<SnapName, number> = $derived({
        closed: 0,
        auto:   auto_height,
        full:   max_height,
    })

    const max_open_height = $derived(snap_heights[snap_names[snap_names.length - 1]])


    /* ------------------------------------------------------------------ */
    /*  state                                                              */
    /* ------------------------------------------------------------------ */

    let current_name: SnapName = $state("closed")

    /* the height the sheet had when it was closed, so it slides away without resizing. */
    let rest_height: number | null = $state(null)

    /* raw finger-driven height while dragging. can go below the auto height. */
    let drag_raw: number | null = $state(null)

    /* the single source of truth for the sheet's height. */
    const visible_height = $derived.by( () =>
    {
        if (drag_raw !== null)
        {
            return Math.min(max_open_height, Math.max(auto_height, drag_raw))
        }

        if (current_name === "closed")
        {
            return rest_height ?? auto_height
        }

        return snap_heights[current_name]
    })

    /* how far the sheet is pulled below its auto height (translateY, in px). */
    const drag_overshoot = $derived(
        drag_raw === null ? 0 : Math.max(0, auto_height - drag_raw)
    )

    const is_body_overflowing = $derived(natural_height > visible_height + 1)


    /* ------------------------------------------------------------------ */
    /*  dragging                                                           */
    /* ------------------------------------------------------------------ */

    let sheet: HTMLElement
    let body_element: HTMLElement

    type Gesture = "idle" | "pending" | "dragging" | "rejected"

    let gesture:         Gesture       = "idle"
    let pointer_id:      number | null = null
    let start_pointer_x  = 0
    let start_pointer_y  = 0
    let start_height     = 0
    let should_suppress_click = false
    let history: { time: number, y: number }[] = []
    let is_starting_in_body = false

    function onPointerDown(event: PointerEvent)
    {
        const target = event.target as HTMLElement

        if (target.closest(TEXT_ENTRY_SELECTOR))
        {
            return
        }

        /* inside a scrollable list the gesture is decided later, at the list's edge. */
        is_starting_in_body = is_body_overflowing && body_element.contains(target)
        
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
        history         = [{ time: event.timeStamp, y: event.clientY }]
    }


    function isVerticalSwipe(event: PointerEvent)
    {
        const delta_x = event.clientX - start_pointer_x
        const delta_y = event.clientY - start_pointer_y

        return Math.abs(delta_x) <= Math.abs(delta_y) * CONE_TAN
    }


    function hasPassedDragThreshold(event: PointerEvent)
    {
        return Math.hypot(event.clientX - start_pointer_x, event.clientY - start_pointer_y) >= DRAG_THRESHOLD
    }

    /* a scrolled list hands the gesture to the sheet only at its edge: pulling down at the top,
    or pushing up at the bottom while the sheet can still grow. */
    function canDragFromBody(delta_y: number)
    {
        if (delta_y > 0)
        {
            return body_element.scrollTop <= 0
        }

        const is_at_bottom = body_element.scrollTop + body_element.clientHeight >= body_element.scrollHeight - 1

        return is_at_bottom && visible_height < max_open_height - 1
    }


    function shouldDragSheet(event: PointerEvent)
    {
        if (!isVerticalSwipe(event))
        {
            return false
        }

        if (!is_starting_in_body)
        {
            return true
        }

        return canDragFromBody(event.clientY - start_pointer_y)
    }


    function onPointerMove(event: PointerEvent)
    {
        if (event.pointerId !== pointer_id)
        {
            return
        }

        if (gesture === "pending")
        {
            if (!hasPassedDragThreshold(event))
            {
                return
            }

            /* decided once, when the threshold is crossed. */
            if (shouldDragSheet(event))
            {
                beginDrag(event)
            }
            else
            {
                gesture = "rejected"
            }

            return
        }

        if (gesture !== "dragging")
        {
            return
        }

        /* swiping down shrinks the sheet, so the sign is flipped. */
        drag_raw = Math.min(max_open_height, Math.max(0, start_height - (event.clientY - start_pointer_y)))

        /* in-place trim: no allocation per move. */
        history.push({ time: event.timeStamp, y: event.clientY })

        while (history.length > 2 && history[0].time < event.timeStamp - VELOCITY_WINDOW)
        {
            history.shift()
        }
    }


    /* px/ms, positive = moving down. */
    function releaseVelocity()
    {
        const first_record = history[0]
        const last_record  = history[history.length - 1]
        const time_delta   = last_record.time - first_record.time

        if (time_delta <= 0)
        {
            return 0
        }

        return (last_record.y - first_record.y) / time_delta
    }


    function targetSnap(release_height: number, velocity: number): SnapName
    {
        /* fling: go to the next snap point in the fling direction. */
        if (velocity > FLING_THRESHOLD)
        {
            return snap_names.findLast( name => snap_heights[name] < release_height - 1 ) ?? "closed"
        }

        if (velocity < -FLING_THRESHOLD)
        {
            return snap_names.find( name => snap_heights[name] > release_height + 1 ) ?? snap_names[snap_names.length - 1]
        }

        /* otherwise: go to the closest snap point. */
        return snap_names.reduce( (closest, name) =>
            Math.abs(snap_heights[name] - release_height) < Math.abs(snap_heights[closest] - release_height)
                ? name
                : closest
        )
    }


    function onPointerUp(event: PointerEvent)
    {
        if (event.pointerId !== pointer_id)
        {
            return
        }

        pointer_id = null

        const was_dragging = gesture === "dragging"

        gesture = "idle"

        /* it never became a drag: either a plain click or a swipe we ignored. */
        if (!was_dragging)
        {
            return
        }

        /* swallow the click the browser may fire after a drag. */
        should_suppress_click = true
        setTimeout( () => should_suppress_click = false, 0 )

        setSnap(targetSnap(drag_raw ?? visible_height, releaseVelocity()))
    }


    function onClickCapture(event: MouseEvent)
    {
        if (!should_suppress_click)
        {
            return
        }

        should_suppress_click = false
        event.stopPropagation()
        event.preventDefault()
    }

    function onKeydown(event: KeyboardEvent)
    {
        if (event.key === "Escape")
        {
            setSnap("closed")
        }
    }


    function setSnap(name: SnapName, should_notify: boolean = true)
    {
        if (name === "closed")
        {
            /* remember where we were, so the sheet slides out at that height. */
            rest_height = visible_height
        }
        else if (current_name === "closed")
        {
            /* a fresh opening starts from the current content height. */
            peak_height = 0
        }

        drag_raw     = null
        current_name = name

        if (should_notify)
        {
            is_open = name !== "closed"
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



    /* once the sheet owns a touch gesture, the list must not scroll along with it. */
    $effect( () =>
    {
        const preventScroll = (event: TouchEvent) =>
        {
            if (gesture === "dragging" && event.cancelable)
            {
                event.preventDefault()
            }
        }

        sheet.addEventListener("touchmove", preventScroll, { passive: false })

        return () => sheet.removeEventListener("touchmove", preventScroll)
    })


    /* sync `is_open` with internal snap changes. external changes have control. */
    $effect( () =>
    {
        const is_opening = is_open

        untrack( () =>
        {
            if (is_opening && current_name === "closed")
            {
                setSnap("auto", false)
            }
            else if (!is_opening && current_name !== "closed")
            {
                setSnap("closed", false)
            }
        })
    })

</script>



<scrim
    role              = "presentation"
    bind:clientHeight = { scrim_height }
    class:opened      = { is_open }
    class:closed      = { !is_open }
    onclick           = { () => setSnap("closed") }
>
    <bottom-sheet
        bind:this       = { sheet }
        role            = "presentation"
        style:height    = "{visible_height}px"
        style:transform = { drag_raw !== null ? `translateY(${drag_overshoot}px)` : undefined }
        class:opened    = { is_open }
        class:closed    = { !is_open }
        class:dragging  = { drag_raw !== null }
        class:resizing  = { is_scrim_resizing }
        onclickcapture  = { onClickCapture }
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
            bind:this          = { body_element }
            class = {[ "body", class_name ]} 
            class:scrollable = { is_body_overflowing }
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

        /* layout inside the sheet never leaks out to the page while it resizes. */
        contain: layout paint;

        /* `height` is set from script. the sheet is anchored to the bottom of the
           screen, so the footer stays put while the content grows and shrinks. */
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
            transition: transform 200ms cubic-bezier(0.05, 0.7, 0.1, 1);
        }

        /* the height snaps to the keyboard; opening and closing still slide. */
        &.opened.resizing
        {
            transition: transform 250ms cubic-bezier(0.05, 0.7, 0.1, 1);
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
        touch-action: none;

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