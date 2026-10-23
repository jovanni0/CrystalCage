<script lang="ts">
    import type { Snippet }              from "svelte";
    import type { HTMLAnchorAttributes, 
                  HTMLButtonAttributes } from "svelte/elements";
    import      { rippleOn }             from "./ripple-effect";

    import      "./ripple-effect.css"



    type Common = {
        label:        string
        is_current:   boolean
        is_collapsed: boolean
        children:     Snippet
    }



    let {
        label,
        is_current,
        is_collapsed,
        children,
        href,
    }: Common & (
        | HTMLAnchorAttributes & { href: string }
        | HTMLButtonAttributes & { href?: undefined}
    ) = $props()
</script>



<svelte:element
    this            = { href ? "a" : "button" }
    href            = { href }
    type            = { href ? undefined : "button" }
    aria-current    = { is_current ? "page" : undefined }
    class:selected  = { is_current }
    style:--_layout-default = { is_collapsed ? "collapsed" : "expanded" }
    
    {@attach rippleOn("indicator-element")}
>
    <indicator-element class = "ripple-container"></indicator-element>
    <icon-wrapper>{@render children?.()}</icon-wrapper>
    <p class = "button-label">{ label }</p>
</svelte:element>



<style>
    a,
    button
    {
        --_layout: var(--nav-item-layout, var(--_layout-default));

        display: grid;
        grid-template-columns: auto 1fr;
        align-items: center;

        padding: 16px;

        user-select: none;
        cursor: pointer;
        overflow: hidden;

        text-decoration: none;
        color: var(--text);
        
        icon-wrapper
        {
            display: flex;
            flex-direction: row;
            align-items: center;

            padding: 12px 4px 12px 24px;

            grid-row: 1;
            grid-column: 1;
        }

        p.button-label
        {
            grid-row: 1;
            grid-column: 2;
            
            padding: 12px 24px 12px 4px;

            font-size: var(--text);
            font-weight: 500;
            text-align: center;
            line-height: 1;
        }

        indicator-element
        {
            display: block;
            grid-row: 1;
            grid-column: 1/3;
            align-self: stretch;
            border-radius: 999px;
            background-color: transparent;
            z-index: -1;
        }

        @container style(--_layout: compressed)
        {
            grid-template-columns: 1fr;
            grid-template-rows: 1fr auto;

            gap: 4px;
            padding: 6px;

            icon-wrapper
            {
                grid-row: 1;
                grid-column: 1;

                padding: 4px 16px;
            }

            p.button-label
            {
                grid-row: 2;
                grid-column: 1;

                padding: 0;

                font-size: var(--text-caption);
            }

            indicator-element
            {
                grid-row: 1;
                grid-column: 1;
            }
        }
    }

    a:hover,
    button:hover
    {
        color: var(--text-brand);
    }


    a.selected,
    button.selected
    {
        color: var(--text-brand);
        
        indicator-element
        {
            background-color: #d9e8d7;
        }

        p.button-label
        {
            font-weight: 600;
        }
    }
</style>