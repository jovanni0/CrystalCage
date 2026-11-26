import type { Attachment } from "svelte/attachments"



export type SwipeDirection = -1 | 1

type SwipeStart =
{
    x:          number
    y:          number
    time:       number
    pointer_id: number
}



const MIN_DISTANCE_PX = 60
const MAX_DURATION_MS = 600
const AXIS_RATIO      = 1.5



/*
*   Reports a quick horizontal swipe on the element: `1` for a swipe to the left (next), `-1` to the right (previous).
*   Sets `touch-action: pan-y`, so vertical scrolling stays native; when the browser takes a touch for scrolling it sends
*   `pointercancel`, which drops the swipe.
*/
export function horizontalSwipe(onSwipe: (direction: SwipeDirection) => void): Attachment<HTMLElement>
{
    return (element) =>
    {
        let start: SwipeStart | null = null


        function onDown(event: PointerEvent): void
        {
            if (!event.isPrimary)
            {
                return
            }

            start = { x: event.clientX, y: event.clientY, time: event.timeStamp, pointer_id: event.pointerId }
        }


        function onUp(event: PointerEvent): void
        {
            if (start === null || event.pointerId !== start.pointer_id)
            {
                return
            }

            const delta_x  = event.clientX - start.x
            const delta_y  = event.clientY - start.y
            const duration = event.timeStamp - start.time

            start = null

            const is_long_enough = Math.abs(delta_x) >= MIN_DISTANCE_PX
            const is_horizontal  = Math.abs(delta_x) > Math.abs(delta_y) * AXIS_RATIO
            const is_quick       = duration <= MAX_DURATION_MS

            if (is_long_enough && is_horizontal && is_quick)
            {
                onSwipe(delta_x < 0 ? 1 : -1)
            }
        }


        function onCancel(): void
        {
            start = null
        }

        element.style.touchAction = "pan-y"
        element.addEventListener("pointerdown", onDown)
        element.addEventListener("pointerup", onUp)
        element.addEventListener("pointercancel", onCancel)

        return () =>
        {
            element.removeEventListener("pointerdown", onDown)
            element.removeEventListener("pointerup", onUp)
            element.removeEventListener("pointercancel", onCancel)
        }
    }
}