<script lang="ts">
    import { onMount, type Snippet } from "svelte";



    // type SnapPoint = {
    //     name:   string
    //     offset: number
    // } | {
    //     name:    "auto"
    //     is_auto: true
    // }

    type SnapPoint = {
        name:   string
        offset: number
    }

    let {
        is_open = $bindable(false),
        children,
        lock_to_fit = false,
    }: {
        is_open?:  boolean
        children?: Snippet
        lock_to_fit?: boolean
    } = $props()


    // const snap_options: SnapPoint[] = [
    //     { name: "auto",   is_auto: true },
    //     { name: "full",   offset: 0     },
    //     { name: "large",  offset: 0.25  },
    //     { name: "half",   offset: 0.5   },
    //     { name: "small",  offset: 0.75  },
    //     { name: "closed", offset: 1     },
    // ]

    let sheet_height:   number        = $state(0)
    let content_height: number        = $state(0)

    let auto_offset: number = $derived.by( () =>
    {
        if (!sheet_height || !content_height)
        {
            return 0.5
        }
        
        const adjusted_content_height = content_height + 36 + 24

        const visible_ratio = Math.min(1, adjusted_content_height / sheet_height)
        const invisible_ration = 1 - visible_ratio

        return invisible_ration
    })

    const snap_options = $derived(
        lock_to_fit 
        ? [
            { name: "auto",   offset: auto_offset },
            { name: "closed", offset: 1 },
        ]
        : [
            { name: "full",   offset: 0     },
            { name: "half",   offset: 0.5   },
            { name: "closed", offset: 1     },
        ]
    )


    const snap_points          = $derived(snap_options.toSorted((a, b) => a.offset - b.offset))
    const snap_position_closed = $derived(snap_points[snap_points.length - 1])
    const snap_position_half   = $derived(snap_points.find(it => it.name === "half") ?? snap_options[0])

    $effect( () =>
    {
        console.log(snap_points)
    })




    let sheet: HTMLElement

    /* the minimum acceleration in px/s that triggers snapping to the next point. */
    const FLING_THRESHOLD: number = 0.5

    let current_snap:   SnapPoint     = $state({ name: "closed", offset: 1 })
    let drag_offset:    number | null = $state(null)


    let pointer_id: number | null = null
    let start_pointer_y = 0
    let start_translate_y = 0
    let history: { time: number, y: number }[] = []


    function offsetToPX(offset: number)
    {
        return offset * sheet_height
    }

    function currentTranslateY()
    {
        if (drag_offset !== null)
        {
            return drag_offset
        }

        return offsetToPX(current_snap.offset)
    }

    function onPointerDown(event: PointerEvent)
    {
        const target = event.target as HTMLElement

        if (target.closest("button, a, input, select, textarea")) 
        {
            return
        }
        
        pointer_id = event.pointerId

        sheet.setPointerCapture(pointer_id)

        start_pointer_y = event.clientY
        start_translate_y = currentTranslateY()
        drag_offset = start_translate_y

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

        const swipe_delta = event.clientY - start_pointer_y
        const translate_delta = start_translate_y + swipe_delta
        const min_offset = Math.min(...snap_points.map(it => offsetToPX(it.offset)))
        const max_offset = Math.max(...snap_points.map(it => offsetToPX(it.offset)))

        drag_offset = Math.min(max_offset, Math.max(min_offset, translate_delta))

        history.push({ 
            time: performance.now(),
            y:    event.clientY
        })

        const history_cutoff_time = performance.now() - 100

        history = history.filter(it => it.time >= history_cutoff_time)
    }


    function onPointerUp(event: PointerEvent)
    {
        if (event.pointerId !== pointer_id)
        {
            return
        }

        pointer_id = null

        const release_position = drag_offset ?? currentTranslateY()
        let velocity = 0

        if (history.length >= 2)
        {
            const first_record = history[0]
            const last_record = history[history.length - 1]
            const history_time_delta = last_record.time - first_record.time

            if (history_time_delta > 0)
            {
                velocity = (last_record.y - first_record.y) / history_time_delta
            }
        }

        let target_snap: SnapPoint

        // snap to the next point in the fling direction
        if (Math.abs(velocity) > FLING_THRESHOLD)
        {
            const current_offset_px = release_position
            const current_snap_index = snap_points.findIndex(it => offsetToPX(it.offset) >= current_offset_px)

            if (velocity > 0)
            {
                const next_snap_index = Math.min(snap_points.length - 1, Math.max(0, current_snap_index))

                target_snap = snap_points[next_snap_index]

                if (offsetToPX(target_snap.offset) <= current_offset_px + 1 && current_snap_index < snap_points.length - 1)
                {
                    target_snap = snap_points[current_snap_index + 1]
                }
            }
            else
            {
                const target_snap_index = current_snap_index <= 0 ? 0 : current_snap_index - 1

                target_snap = snap_points[target_snap_index]
            }
        }
        // snap to the closest point
        else
        {
            target_snap = snap_points.reduce( (closest, it) =>
            {
                const next_point_position = offsetToPX(it.offset)
                const closest_point_position = offsetToPX(closest.offset)

                const next_delta = Math.abs(next_point_position - release_position)
                const closest_delta = Math.abs(closest_point_position - release_position)

                return next_delta < closest_delta ? it : closest
            })

        }

        setSnap(target_snap)
    }



    function onKeydown(event: KeyboardEvent)
    {
        if (event.key === "Escape")
        {
            setSnap(snap_position_closed)
        }
    }


    function setSnap(snap_point: SnapPoint, notify_external: boolean = true)
    {
        drag_offset = null
        current_snap = snap_point

        if (notify_external)
        {
            is_open = snap_point.name !== "closed"
        }
    }


    onMount( () =>
    {
        sheet_height = sheet.offsetHeight

        const resize_observer = new ResizeObserver( () =>
        {
            sheet_height = sheet.offsetHeight
        })

        resize_observer.observe(sheet)

        return () => resize_observer.disconnect()
    })


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
        if (is_open && current_snap.name === "closed")
        {
            setSnap(snap_position_half, false)
        }
        else if (!is_open && current_snap.name !== "closed")
        {
            setSnap(snap_position_closed, false)
        }
    })
</script>




<scrim
    role         = "presentation"
    class:opened = { is_open }
    class:closed = { !is_open }
    onclick      = { () => setSnap(snap_position_closed) }
>
    <bottom-sheet 
        bind:this         = { sheet }
        role              = "presentation"
        style:transform   = "translateY({currentTranslateY()}px)"
        style:height      = { lock_to_fit ? `${content_height}px` : undefined }
        class:opened      = { is_open }
        class:closed      = { !is_open }
        class:fit-content = { lock_to_fit }
        onclick           = { (event: MouseEvent) => event.stopPropagation() }
        onpointerdown     = { onPointerDown }
        onpointermove     = { onPointerMove }
        onpointerup       = { onPointerUp }
        onpointercancel   = { onPointerUp }
    >
        <div class = "thumb"></div>

        <wrapper
            bind:clientHeight = { content_height }
        >
            {@render children?.()}
        </wrapper>
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
                opacity 200ms cubic-bezier(0.05, 0.7, 0.1, 1),
                visibility 0ms linear 200ms;

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
        height: 90vh;
        overflow-y: hidden;

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
                height 700ms cubic-bezier(0.05, 0.7, 0.1, 1),
                transform 250ms cubic-bezier(0.05, 0.7, 0.1, 1);
            transform: translateY(0);
        }

        &.closed
        {
            transition: 
                height 700ms cubic-bezier(0.05, 0.7, 0.1, 1),
                transform 200ms cubic-bezier(0.05, 0.7, 0.1, 1);
            transform: translateY(100%);
        }

        &.fit-content
        {
            max-height: 90vh;
            justify-content: flex-end;
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

    wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        height: 100%;
        min-height: 0;
        box-sizing: border-box;
        overflow: hidden;
    }
</style>