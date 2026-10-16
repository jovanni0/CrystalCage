<script lang="ts">
    import type { Heading } from "#lib/content/markdown.ts";



    // header height plus some breathing room: a heading counts as "current" once it passes this line
    const ACTIVE_LINE_OFFSET = 120



    let {
        headings
    }: {
        headings: Heading[]
    } = $props()


    let active_id: string | null = $state(null)


    $effect(() =>
    {
        const heading_elements = headings
            .map((it) => document.getElementById(it.id))
            .filter((it): it is HTMLElement => it !== null)

        function updateActiveHeading()
        {
            const is_at_bottom = window.innerHeight + window.scrollY >= document.documentElement.scrollHeight - 2

            if (is_at_bottom)
            {
                active_id = heading_elements.at(-1)?.id ?? null
                return
            }

            let current_id: string | null = heading_elements[0]?.id ?? null

            for (const element of heading_elements)
            {
                if (element.getBoundingClientRect().top > ACTIVE_LINE_OFFSET)
                {
                    break
                }

                current_id = element.id
            }

            active_id = current_id
        }

        updateActiveHeading()
        window.addEventListener("scroll", updateActiveHeading, { passive: true })

        return () =>
        {
            window.removeEventListener("scroll", updateActiveHeading)
        }
    })


    function currentFor(heading_id: string): "location" | undefined
    {
        return active_id === heading_id ? "location" : undefined
    }
</script>



<nav
    aria-label = "on this page"
>
    <ul>
        {#each headings as heading (heading.id)}
            <li>
                <a 
                    data-depth = { heading.depth }
                    href         = "#{ heading.id }"
                    class        = "toc-element"
                    aria-current = { currentFor(heading.id) }
                >
                    { heading.text }
                </a>
            </li>
        {/each}
    </ul>
</nav>



<style>
    nav
    {
        position: sticky;
        top: 0;
        align-self: start;
        max-height: 100%;
        overflow-y: auto;

        display: flex;
        flex-direction: column;
        padding: 32px 16px;

        ul
        {
            margin: 0;
            padding: 0;
            list-style: none;

            a
            {
                display: flex;
                padding: 12px 24px;
                border-radius: 999px;

                color: var(--text);
                text-decoration: none;

                &:hover
                {
                    background-color: rgba(0, 0, 0, 0.1);
                }

                &[aria-current="location"]
                {
                    font-weight: 800;
                }

                &[data-depth="3"]
                {
                    position: relative;
                    margin-left: 36px;
                    padding-left: 12px;
                    border-radius: 0 999px 999px 0;

                    &:before
                    {
                        content: "";
                        width: 1px;
                        position: absolute;
                        inset: 4px auto 4px 0;
                        background-color: var(--text-placeholder);
                    }
                }
            }
        }
    }
</style>