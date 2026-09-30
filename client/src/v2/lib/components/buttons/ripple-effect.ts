import type { Attachment } from "svelte/attachments";



export const ripple_effect: Attachment<HTMLElement> = (element) =>
{
    function start()
    {
        if (element.matches(":disabled, [aria-disabled='true'], [aria-busy='true']"))
        {
            return
        }

        element.removeAttribute("ripple-effect")
        void element.offsetWidth
        element.setAttribute("ripple-effect", "")
    }


    function end(event: AnimationEvent)
    {
        if (event.pseudoElement === "::before")
        {
            element.removeAttribute("ripple-effect")
        }
    }


    element.addEventListener("pointerdown", start)
    element.addEventListener("animationend", end)

    return () => 
    {
        element.removeEventListener("pointerdown", start)
        element.removeEventListener("animationend", end)
    }
}