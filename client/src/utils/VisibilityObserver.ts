


export class VisibilityObserver
{
    private callbacks = new WeakMap<Element, () => void>()
    private observer = new IntersectionObserver(
        (entries) => 
        {
            for (const entry of entries)
            {
                if (entry.isIntersecting)
                {
                    this.callbacks.get(entry.target)?.()
                    this.unobserve(entry.target)
                }
            }
        },
        { rootMargin: "200px" }
    )


    observe(element: Element, callback: () => void)
    {
        this.callbacks.set(element, callback)
        this.observer.observe(element)
    }


    unobserve(element: Element)
    {
        this.observer.unobserve(element)
        this.callbacks.delete(element)
    }
}