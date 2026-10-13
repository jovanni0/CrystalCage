import type { Attachment }           from "svelte/attachments"
import type { PageDirection,
              TapRecord }            from "../rules/gesture-rules"
import type { Point,
              Size,
              ZoomTransform }        from "../rules/zoom-rules"
import      { DOUBLE_TAP_DELAY_MS,
              GestureRules,
              ZOOM_SNAP_BACK_SCALE } from "../rules/gesture-rules"
import      { DOUBLE_TAP_ZOOM_SCALE,
              ZoomRules }            from "../rules/zoom-rules"



type ActiveGesture = "none" | "undecided" | "pan" | "pinch" | "page" | "dismiss"



export type ViewerGestureOptions =
{
    getContainer:  () => Size
    getFittedSize: () => Size
    canPage:       (direction: PageDirection) => boolean
    onPage:        (direction: PageDirection) => void
    onDismiss:     () => void
    onTap:         () => void
}



/*
* Pointer gestures of a swipeable, zoomable viewer: pan, pinch and double-tap zoom on the current item, horizontal swipe between items,
* swipe down to dismiss, single tap. Owns the on-screen state (zoom transform, strip offsets, settle animation); the owner decides what
* paging, dismissing and tapping mean.
*/
export class ViewerGestures
{
    public transform:        ZoomTransform = $state.raw(ZoomRules.IDENTITY)
    public page_offset_x:    number        = $state(0)
    public dismiss_offset_y: number        = $state(0)
    public is_settling:      boolean       = $state(false)

    // bookkeeping read only by the handlers, so not reactive
    private pointers                                              = new Map<number, Point>()
    private gesture:         ActiveGesture                        = "none"
    private stage_rect:      DOMRect | null                       = null
    private start_point:     Point                                = { x: 0, y: 0 }
    private start_time:      number                               = 0
    private start_transform: ZoomTransform                        = ZoomRules.IDENTITY
    private pinch_distance:  number                               = 1
    private pinch_midpoint:  Point                                = { x: 0, y: 0 }
    private last_tap:        TapRecord | null                     = null
    private tap_timer:       ReturnType<typeof setTimeout> | null = null


    constructor(private options: ViewerGestureOptions)
    {}


    /*
    * `{@attach gestures.attach}` on the element that receives the touches. Sets `touch-action: none`, without which Android's WebView
    * claims the touch for its own scrolling and zooming and sends `pointercancel`.
    */
    public readonly attach: Attachment<HTMLElement> = (stage) =>
    {
        const onDown   = (event: PointerEvent): void => this.onPointerDown(stage, event)
        const onMove   = (event: PointerEvent): void => this.onPointerMove(stage, event)
        const onUp     = (event: PointerEvent): void => this.onPointerUp(stage, event)
        const onCancel = (event: PointerEvent): void => this.onPointerCancel(event)

        stage.style.touchAction = "none"
        stage.addEventListener("pointerdown", onDown)
        stage.addEventListener("pointermove", onMove)
        stage.addEventListener("pointerup", onUp)
        stage.addEventListener("pointercancel", onCancel)

        return () =>
        {
            stage.removeEventListener("pointerdown", onDown)
            stage.removeEventListener("pointermove", onMove)
            stage.removeEventListener("pointerup", onUp)
            stage.removeEventListener("pointercancel", onCancel)
            this.cancelPendingTap()
        }
    }


    /*
    * Moves to the neighbouring item, or springs back when there is none. Changing the index and clearing the drag offset in the same
    * update lets the strip's transform transition carry the slide from where the finger left it to its resting place.
    */
    public page(direction: PageDirection): void
    {
        this.is_settling   = true
        this.page_offset_x = 0

        if (!this.options.canPage(direction))
        {
            return
        }

        this.transform = ZoomRules.IDENTITY
        this.options.onPage(direction)
    }


    private pointOf(stage: HTMLElement, event: PointerEvent): Point
    {
        if (this.stage_rect === null)
        {
            this.stage_rect = stage.getBoundingClientRect()
        }

        return {
            x: event.clientX - this.stage_rect.left - this.stage_rect.width / 2,
            y: event.clientY - this.stage_rect.top - this.stage_rect.height / 2,
        }
    }


    private onPointerDown(stage: HTMLElement, event: PointerEvent): void
    {
        stage.setPointerCapture(event.pointerId)

        this.stage_rect  = null
        this.is_settling = false

        const point = this.pointOf(stage, event)

        this.pointers.set(event.pointerId, point)

        if (this.pointers.size === 1)
        {
            this.startSinglePointer(point, true)
            return
        }

        if (this.pointers.size === 2)
        {
            this.startPinch()
        }
    }


    private startSinglePointer(point: Point, can_be_tap: boolean): void
    {
        this.start_point     = point
        this.start_time      = performance.now()
        this.start_transform = this.transform

        if (can_be_tap)
        {
            this.gesture = "undecided"
            return
        }

        // the finger left on screen after a pinch: never a tap, keeps panning if zoomed
        this.gesture = this.transform.scale > 1 ? "pan" : "none"
    }


    private startPinch(): void
    {
        const [ first, second ] = [ ...this.pointers.values() ]

        this.page_offset_x    = 0
        this.dismiss_offset_y = 0
        this.start_transform  = this.transform
        this.pinch_distance   = Math.max(1, ZoomRules.distance(first, second))
        this.pinch_midpoint   = ZoomRules.midpoint(first, second)
        this.gesture          = "pinch"
    }


    private onPointerMove(stage: HTMLElement, event: PointerEvent): void
    {
        if (!this.pointers.has(event.pointerId))
        {
            return
        }

        const point = this.pointOf(stage, event)
        const delta = { x: point.x - this.start_point.x, y: point.y - this.start_point.y }

        this.pointers.set(event.pointerId, point)

        if (this.gesture === "undecided")
        {
            this.gesture = GestureRules.decide(delta, this.transform.scale > 1)
        }

        this.applyGesture(delta)
    }


    private applyGesture(delta: Point): void
    {
        if (this.gesture === "pinch")
        {
            this.updatePinch()
        }
        else if (this.gesture === "pan")
        {
            const panned = { scale: this.start_transform.scale, x: this.start_transform.x + delta.x, y: this.start_transform.y + delta.y }

            this.transform = ZoomRules.clampOffset(panned, this.options.getFittedSize(), this.options.getContainer())
        }
        else if (this.gesture === "page")
        {
            this.page_offset_x = GestureRules.stripOffsetFor(delta.x, this.options.canPage(-1), this.options.canPage(1))
        }
        else if (this.gesture === "dismiss")
        {
            this.dismiss_offset_y = Math.max(0, delta.y)
        }
    }


    private updatePinch(): void
    {
        const [ first, second ] = [ ...this.pointers.values() ]
        const midpoint = ZoomRules.midpoint(first, second)
        const scale    = this.start_transform.scale * ZoomRules.distance(first, second) / this.pinch_distance
        const zoomed   = ZoomRules.zoomAround(this.start_transform, scale, this.pinch_midpoint)

        this.transform = ZoomRules.clampOffset({
            scale: zoomed.scale,
            x:     zoomed.x + midpoint.x - this.pinch_midpoint.x,
            y:     zoomed.y + midpoint.y - this.pinch_midpoint.y,
        }, this.options.getFittedSize(), this.options.getContainer())
    }


    private onPointerUp(stage: HTMLElement, event: PointerEvent): void
    {
        if (!this.pointers.has(event.pointerId))
        {
            return
        }

        this.pointers.delete(event.pointerId)

        if (this.gesture === "pinch" && this.pointers.size === 1)
        {
            const [ remaining ] = [ ...this.pointers.values() ]

            this.startSinglePointer(remaining, false)
            return
        }

        if (this.pointers.size === 0)
        {
            this.finishGesture(this.pointOf(stage, event))
        }
    }


    private onPointerCancel(event: PointerEvent): void
    {
        this.pointers.delete(event.pointerId)

        if (this.pointers.size > 0)
        {
            return
        }

        this.gesture          = "none"
        this.is_settling      = true
        this.page_offset_x    = 0
        this.dismiss_offset_y = 0
        this.settleZoom()
    }


    private finishGesture(point: Point): void
    {
        const elapsed_ms = Math.max(1, performance.now() - this.start_time)
        const delta      = { x: point.x - this.start_point.x, y: point.y - this.start_point.y }
        const finished   = this.gesture

        this.gesture     = "none"
        this.is_settling = true

        if (finished === "undecided")
        {
            this.handleTap(point)
        }
        else if (finished === "page")
        {
            this.finishPageSwipe(delta.x, delta.x / elapsed_ms)
        }
        else if (finished === "dismiss")
        {
            this.finishDismiss(delta.y, delta.y / elapsed_ms)
        }
        else if (finished === "pan" || finished === "pinch")
        {
            this.settleZoom()
        }
    }


    private settleZoom(): void
    {
        if (this.transform.scale < ZOOM_SNAP_BACK_SCALE)
        {
            this.transform = ZoomRules.IDENTITY
        }
    }


    private finishPageSwipe(delta_x: number, velocity_x: number): void
    {
        const direction = GestureRules.pageDirectionFor(delta_x, velocity_x, this.options.getContainer().width)

        if (direction === null)
        {
            this.page_offset_x = 0
            return
        }

        this.page(direction)
    }


    private finishDismiss(delta_y: number, velocity_y: number): void
    {
        if (GestureRules.isDismissed(delta_y, velocity_y))
        {
            this.options.onDismiss()
            return
        }

        this.dismiss_offset_y = 0
    }


    /*
    * A single tap waits for a possible second tap before it is reported, so a double tap does not also count as a tap.
    */
    private handleTap(point: Point): void
    {
        const tap = { point: point, time: performance.now() }

        if (GestureRules.isDoubleTap(this.last_tap, tap))
        {
            this.cancelPendingTap()
            this.last_tap = null
            this.toggleZoomAt(point)
            return
        }

        this.last_tap  = tap
        this.tap_timer = setTimeout(() =>
        {
            this.tap_timer = null
            this.options.onTap()
        }, DOUBLE_TAP_DELAY_MS)
    }


    private cancelPendingTap(): void
    {
        if (this.tap_timer !== null)
        {
            clearTimeout(this.tap_timer)
            this.tap_timer = null
        }
    }


    private toggleZoomAt(point: Point): void
    {
        if (this.transform.scale > 1)
        {
            this.transform = ZoomRules.IDENTITY
            return
        }

        const zoomed = ZoomRules.zoomAround(this.transform, DOUBLE_TAP_ZOOM_SCALE, point)

        this.transform = ZoomRules.clampOffset(zoomed, this.options.getFittedSize(), this.options.getContainer())
    }
}