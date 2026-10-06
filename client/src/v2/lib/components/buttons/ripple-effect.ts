import type { Attachment } from "svelte/attachments";



type RippleState = "pressed" | "released" | "cancelled" | "tapped"

const PRESS_DELAY_MS   = 100
const MOVE_SLOPE_PX    = 8
const IGNORED_ELEMENTS = ":disabled, [aria-disabled='true'], [aria-busy='true']"



export const ripple_effect: Attachment<HTMLElement> = (element) =>
{
    let press_timer: ReturnType<typeof setTimeout> | null = null
    let start_x = 0
    let start_y = 0


    function setState(state: RippleState): void
    {
        element.removeAttribute("ripple-state")

        void element.offsetWidth

        element.setAttribute("ripple-state", state)
    }


    function setOrigin(event: PointerEvent)
    {
        const bounding_box = element.getBoundingClientRect()

        const ripple_x = event.clientX - bounding_box.left
        const ripple_y = event.clientY - bounding_box.top
        const ripple_radius = Math.hypot(Math.max(ripple_x, bounding_box.width - ripple_x), Math.max(ripple_y, bounding_box.height - ripple_y))

        element.style.setProperty("--ripple-x", `${ripple_x}px`)
        element.style.setProperty("--ripple-y", `${ripple_y}px`)
        element.style.setProperty("--ripple-radius", `${ripple_radius}px`)
    }


    function clearTimer(): boolean
    {
        if (press_timer === null)
        {
            return false
        }

        clearTimeout(press_timer)
        press_timer = null

        return true
    }


    function isEventInside(event: PointerEvent): boolean
    {
        const bounding_box = element.getBoundingClientRect()

        return event.clientX >= bounding_box.left && event.clientX <= bounding_box.right
            && event.clientY >= bounding_box.top && event.clientY <= bounding_box.bottom
    }


    function stopTracking()
    {
        element.removeEventListener("pointermove", onMove)
    }


    function onDown(event: PointerEvent)
    {
        if (element.matches(IGNORED_ELEMENTS)) return

        start_x = event.clientX
        start_y = event.clientY

        clearTimer()
        setOrigin(event)

        press_timer = setTimeout( () =>
        {
            press_timer = null
            setState("pressed")
        }, PRESS_DELAY_MS)

        element.addEventListener("pointermove", onMove)
    }


    function onMove(event: PointerEvent)
    {
        const distance = Math.hypot(event.clientX - start_x, event.clientY - start_y)

        if (distance > MOVE_SLOPE_PX && clearTimer())
        {
            stopTracking()
        }
    }


    function onUp(event: PointerEvent)
    {
        stopTracking()

        if (clearTimer())
        {
            setState("tapped")
            return
        }

        if (element.hasAttribute("ripple-state"))
        {
            setState(isEventInside(event) ? "released" : "cancelled")
        }
    }


    function onCancel()
    {
        stopTracking()

        if (clearTimer()) return

        if (element.hasAttribute("ripple-state"))
        {
            setState("cancelled")
        }
    }


    function onAnimationEnd(event: AnimationEvent)
    {
        if (event.pseudoElement === "::before" && event.animationName === "ripple-fade")
        {
            element.removeAttribute("ripple-state")
        }
    }


    element.addEventListener("pointerdown",   onDown)
    element.addEventListener("pointerup",     onUp)
    element.addEventListener("pointercancel", onCancel)
    element.addEventListener("animationend",  onAnimationEnd)


    return () =>
    {
        clearTimer()
        stopTracking()
        element.removeEventListener("pointerdown",   onDown)
        element.removeEventListener("pointerup",     onUp)
        element.removeEventListener("pointercancel", onCancel)
        element.removeEventListener("animationend",  onAnimationEnd)
    }
}