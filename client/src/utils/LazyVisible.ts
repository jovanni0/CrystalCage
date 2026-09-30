import { visibility_observer } from "@root/CompositionRoot";



export function lazyVisible(node: HTMLElement, callback: () => void)
{
    visibility_observer.observe(node, callback)

    return {
        destroy()
        {
            visibility_observer.unobserve(node)
        }
    }
}