<script lang="ts">
    import type { Snippet }       from "svelte";
    import      { pullToRefresh } from "@v2/lib/utils/pull-to-refresh";
    import      RotateCw          from "../icons/rotate-cw.svelte";
    import      Spinner           from "./spinner.svelte";


    
    let {
        topbar,
        children,
        navbar,
        sheets,
        class: className,
        onRefresh,
    }: {
        topbar?:    Snippet
        children?:  Snippet
        navbar?:    Snippet
        sheets?:    Snippet
        class?:     string
        onRefresh?: () =>Promise<void>
    } = $props()
</script>



<page>
    <topbar>{@render topbar?.()}</topbar>
    <layout 
        class = { className }
        {@attach pullToRefresh( () => onRefresh ?? null )}
    >
        {#if onRefresh}
            <refresh-indicator aria-hidden = "true">
                <refresh-disc>
                    <span class = "pull-icon">
                        <RotateCw />
                    </span>
                    
                    <span class = "spinner">
                        <Spinner 
                            size = { 24 } 
                            stroke-width = { 2 }
                        />
                    </span>
                </refresh-disc>
            </refresh-indicator>
        {/if}

        {@render children?.()}
    </layout>
    <navbar>{@render navbar?.()}</navbar>
    <sheets>{@render sheets?.()}</sheets>
</page>



<style>
    page
    {
        display: flex;
        flex-direction: column;
        position: relative;
        width: 100%;
        height: 100%;
        box-sizing: border-box;
        padding-top: env(safe-area-inset-top, 0px);
        padding-bottom: env(safe-area-inset-bottom, 0px);
        overflow: hidden;
        background-color: var(--bg);
        timeline-scope: --content-scroll;
    }

    topbar
    {
        border-bottom: 1px solid transparent;
        animation: topbar-divider linear both;
        animation-timeline: --content-scroll;
        animation-range: 0px 8px;
    }

    layout
    {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        overscroll-behavior: none;
        animation: scroll-space linear both;
        animation-timeline: scroll(self y);
        scroll-timeline: --content-scroll y;

        refresh-indicator
        {
            position: sticky;
            top: 0;
            height: 0;
            z-index: 1;

            display: flex;
            justify-content: center;
            pointer-events: none;

            refresh-disc
            {
                display: flex;
                align-items: center;
                justify-content: center;

                width: 40px;
                height: 40px;
                border-radius: 50%;
                background-color: var(--bg-overlay);
                box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);

                opacity: var(--pull-progress, 0);
                transform: translateY(calc(var(--pull-distance, 0px) - 48px));

                & > .pull-icon
                {
                    display: flex;
                    transform: rotate(calc(var(--pull-progress, 0) * 270deg));
                }

                & > .spinner
                {
                    display: none;
                }
            }
        }

        &:global([pull-state = "idle"]) refresh-disc,
        &:global([pull-state = "refreshing"]) refresh-disc
        {
            transition: transform 200ms ease, opacity 200ms ease;
        }

        &:global([pull-state = "refreshing"]) refresh-disc
        {
            & > .pull-icon
            {
                display: none;
            }

            & > .spinner
            {
                display: flex;
            }
        }
    }


    @keyframes scroll-space
    {
        from, to
        {
            padding-bottom: 128px;
        }
    }


    @keyframes topbar-divider
    {
        from
        {
            border-bottom-color: transparent;
        }

        to
        {
            border-bottom-color: var(--text-placeholder);
        }
    }
</style>