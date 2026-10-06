<script lang="ts">
    import type { Snippet } from "svelte";


    
    let {
        topbar,
        children,
        navbar,
        sheets,
        class: className,
    }: {
        topbar?: Snippet
        children?: Snippet
        navbar?: Snippet
        sheets?: Snippet
        class?: string
    } = $props()
</script>



<page>
    <topbar>{@render topbar?.()}</topbar>
    <layout class = { className }>{@render children?.()}</layout>
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
        /* edge-to-edge: the page draws behind the system bars, so keep content out of them */
        padding-top: var(--safe-area-inset-top, env(safe-area-inset-top, 0px));
        padding-bottom: var(--safe-area-inset-bottom, env(safe-area-inset-bottom, 0px));
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