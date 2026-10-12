import type { Attachment } from "svelte/attachments"



type PullState = "idle" | "pulling" | "armed" | "refreshing"

const SLOP_PX      = 8
const RESISTANCE   = 0.5
const THRESHOLD_PX = 72
const MAX_PULL_PX  = 120



/*
* Pull down at the top of a scroll container to refresh.
* `getHandler` is read at release time, so the handler can change without re-attaching.
*/
export function pullToRefresh(getHandler: () => (() => Promise<void>) | null): Attachment<HTMLElement>
{
    return (element) =>
    {
        let start_x       = 0
        let start_y       = 0
        let distance      = 0
        let is_tracking   = false
        let is_pulling    = false
        let is_refreshing = false


        function setState(state: PullState): void
        {
            element.setAttribute("pull-state", state)
        }


        function setDistance(value: number): void
        {
            distance = value

            element.style.setProperty("--pull-distance", `${value}px`)
            element.style.setProperty("--pull-progress", String(Math.min(value / THRESHOLD_PX, 1)))
        }


        function reset(): void
        {
            is_tracking = false
            is_pulling  = false

            setState("idle")
            setDistance(0)
        }


        function onStart(event: TouchEvent): void
        {
            if (is_refreshing || element.scrollTop > 0 || event.touches.length !== 1)
            {
                return
            }

            is_tracking = true
            is_pulling  = false
            start_x     = event.touches[0].clientX
            start_y     = event.touches[0].clientY
        }


        function onMove(event: TouchEvent): void
        {
            if (!is_tracking)
            {
                return
            }

            const delta_x = event.touches[0].clientX - start_x
            const delta_y = event.touches[0].clientY - start_y

            if (!is_pulling && !shouldStartPulling(delta_x, delta_y))
            {
                return
            }

            is_pulling = true
            event.preventDefault()

            const pulled = Math.min(Math.max((delta_y - SLOP_PX) * RESISTANCE, 0), MAX_PULL_PX)

            setDistance(pulled)
            setState(pulled >= THRESHOLD_PX ? "armed" : "pulling")
        }


        /*
        * A horizontal swipe (eg: the carousel) or an upward drag (normal scrolling) ends tracking.
        */
        function shouldStartPulling(delta_x: number, delta_y: number): boolean
        {
            const is_horizontal = Math.abs(delta_x) > SLOP_PX && Math.abs(delta_x) > Math.abs(delta_y)

            if (is_horizontal || delta_y < 0)
            {
                is_tracking = false
                return false
            }

            return delta_y >= SLOP_PX
        }


        async function onEnd(): Promise<void>
        {
            if (!is_pulling)
            {
                is_tracking = false
                return
            }

            const handler = getHandler()

            if (distance < THRESHOLD_PX || handler === null)
            {
                reset()
                return
            }

            is_tracking   = false
            is_pulling    = false
            is_refreshing = true

            setState("refreshing")
            setDistance(THRESHOLD_PX)

            try
            {
                await handler()
            }
            finally
            {
                is_refreshing = false
                reset()
            }
        }


        setState("idle")

        element.addEventListener("touchstart",  onStart, { passive: true })
        element.addEventListener("touchmove",   onMove,  { passive: false })
        element.addEventListener("touchend",    onEnd)
        element.addEventListener("touchcancel", reset)


        return () =>
        {
            element.removeEventListener("touchstart",  onStart)
            element.removeEventListener("touchmove",   onMove)
            element.removeEventListener("touchend",    onEnd)
            element.removeEventListener("touchcancel", reset)
        }
    }
}