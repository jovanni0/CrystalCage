import type { Point } from "./zoom-rules"



export type SwipeDecision = "undecided" | "pan" | "page" | "dismiss" | "none"
export type PageDirection = -1 | 1

export type TapRecord =
{
    point: Point
    time:  number
}



export const GESTURE_DECISION_DISTANCE = 10
export const DOUBLE_TAP_DELAY_MS       = 250
export const DOUBLE_TAP_DISTANCE       = 30
export const PAGE_COMMIT_RATIO         = 0.25
export const DISMISS_COMMIT_DISTANCE   = 120
export const FLICK_VELOCITY            = 0.5      // px per ms
export const EDGE_RESISTANCE           = 3
export const ZOOM_SNAP_BACK_SCALE      = 1.05



export class GestureRules
{
    /*
    *   One finger stays undecided (a tap if released) until it moves far enough. Zoomed, any drag pans the image; at 1× the dominant
    *   axis decides between paging and dismissing, and dragging up does nothing.
    */
    public static decide(delta: Point, is_zoomed: boolean): SwipeDecision
    {
        if (Math.hypot(delta.x, delta.y) < GESTURE_DECISION_DISTANCE)
        {
            return "undecided"
        }

        if (is_zoomed)
        {
            return "pan"
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


    public static isDoubleTap(previous: TapRecord | null, current: TapRecord): boolean
    {
        if (previous === null)
        {
            return false
        }

        const is_quick_enough = current.time - previous.time < DOUBLE_TAP_DELAY_MS
        const is_close_enough = Math.hypot(current.point.x - previous.point.x, current.point.y - previous.point.y) < DOUBLE_TAP_DISTANCE

        return is_quick_enough && is_close_enough
    }


    /*
    * Where a released horizontal swipe goes: far or fast enough commits, otherwise `null` snaps back. Dragging left shows the next item.
    */
    public static pageDirectionFor(delta_x: number, velocity_x: number, width: number): PageDirection | null
    {
        const is_next     = delta_x < -width * PAGE_COMMIT_RATIO || velocity_x < -FLICK_VELOCITY
        const is_previous = delta_x > width * PAGE_COMMIT_RATIO || velocity_x > FLICK_VELOCITY

        if (is_next)
        {
            return 1
        }

        if (is_previous)
        {
            return -1
        }

        return null
    }


    public static isDismissed(delta_y: number, velocity_y: number): boolean
    {
        return delta_y > DISMISS_COMMIT_DISTANCE || velocity_y > FLICK_VELOCITY
    }


    /*
    * Past the first or last item the strip follows the finger at a fraction of its distance, as a "nothing more here" cue.
    */
    public static stripOffsetFor(delta_x: number, can_go_previous: boolean, can_go_next: boolean): number
    {
        const is_past_start = delta_x > 0 && !can_go_previous
        const is_past_end   = delta_x < 0 && !can_go_next

        if (is_past_start || is_past_end)
        {
            return delta_x / EDGE_RESISTANCE
        }

        return delta_x
    }
}