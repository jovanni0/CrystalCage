import { Maths } from "@v2/lib/utils/maths";
import type { Attachment } from "svelte/attachments";



type RippleState = "pressed" | "released" | "cancelled" | "tapped"

const PRESS_DELAY_MS   = 100
const MOVE_SLOPE_PX    = 8
const IGNORED_ELEMENTS = ":disabled, [aria-disabled='true'], [aria-busy='true']"



/*
* Ripple on the element itself.
*/
export const ripple_effect: Attachment<HTMLElement> = (element) =>
{
    return attachRipple(element, element)
}



/*
* Listens on the host element but draws the ripple on a child, eg: the whole navbar tab reacts, only the pill ripples.
* The child needs the `ripple-container` class.
*/
export function rippleOn(surface_selector: string): Attachment<HTMLElement>
{
    return (element) =>
    {
        const surface = element.querySelector<HTMLElement>(surface_selector)

        if (surface === null)
        {
            return
        }

        return attachRipple(element, surface)
    }
}



function attachRipple(trigger: HTMLElement, surface: HTMLElement): () => void
{
    let press_timer: ReturnType<typeof setTimeout> | null = null
    let start_x = 0
    let start_y = 0


    function setState(state: RippleState): void
    {
        surface.removeAttribute("ripple-state")

        void surface.offsetWidth

        surface.setAttribute("ripple-state", state)
    }


    /*
    * The origin is clamped to the surface, so a tap on the label starts the ripple at the pill's nearest edge.
    */
    function setOrigin(event: PointerEvent): void
    {
        const bounding_box = surface.getBoundingClientRect()

        const ripple_x      = Maths.clamp(event.clientX - bounding_box.left, 0, bounding_box.width)
        const ripple_y      = Maths.clamp(event.clientY - bounding_box.top, 0, bounding_box.height)
        const ripple_radius = Math.hypot(Math.max(ripple_x, bounding_box.width - ripple_x), Math.max(ripple_y, bounding_box.height - ripple_y))

        surface.style.setProperty("--ripple-x", `${ripple_x}px`)
        surface.style.setProperty("--ripple-y", `${ripple_y}px`)
        surface.style.setProperty("--ripple-radius", `${ripple_radius}px`)
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
        const bounding_box = trigger.getBoundingClientRect()

        return event.clientX >= bounding_box.left && event.clientX <= bounding_box.right
            && event.clientY >= bounding_box.top && event.clientY <= bounding_box.bottom
    }


    function stopTracking(): void
    {
        trigger.removeEventListener("pointermove", onMove)
    }


    function onDown(event: PointerEvent): void
    {
        if (trigger.matches(IGNORED_ELEMENTS))
        {
            return
        }

        start_x = event.clientX
        start_y = event.clientY

        clearTimer()
        setOrigin(event)

        press_timer = setTimeout(() =>
        {
            press_timer = null
            setState("pressed")
        }, PRESS_DELAY_MS)

        trigger.addEventListener("pointermove", onMove)
    }


    function onMove(event: PointerEvent): void
    {
        const distance = Math.hypot(event.clientX - start_x, event.clientY - start_y)

        if (distance > MOVE_SLOPE_PX && clearTimer())
        {
            stopTracking()
        }
    }


    function onUp(event: PointerEvent): void
    {
        stopTracking()

        if (clearTimer())
        {
            setState("tapped")
            return
        }

        if (surface.hasAttribute("ripple-state"))
        {
            setState(isEventInside(event) ? "released" : "cancelled")
        }
    }


    function onCancel(): void
    {
        stopTracking()

        if (clearTimer())
        {
            return
        }

        if (surface.hasAttribute("ripple-state"))
        {
            setState("cancelled")
        }
    }


    function onAnimationEnd(event: AnimationEvent): void
    {
        if (event.pseudoElement === "::before" && event.animationName === "ripple-fade")
        {
            surface.removeAttribute("ripple-state")
        }
    }


    trigger.addEventListener("pointerdown",   onDown)
    trigger.addEventListener("pointerup",     onUp)
    trigger.addEventListener("pointercancel", onCancel)
    surface.addEventListener("animationend",  onAnimationEnd)


    return () =>
    {
        clearTimer()
        stopTracking()
        trigger.removeEventListener("pointerdown",   onDown)
        trigger.removeEventListener("pointerup",     onUp)
        trigger.removeEventListener("pointercancel", onCancel)
        surface.removeEventListener("animationend",  onAnimationEnd)
    }
}