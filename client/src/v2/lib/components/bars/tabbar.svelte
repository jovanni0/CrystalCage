<script lang="ts">
    import { nav } from "@root/CompositionRoot";
    import type { Tab }      from "@v2/lib/rules/tab-rules";



    let { 
        tabs,
        current_tab_id,
    } : { 
        tabs:           Tab[]
        current_tab_id: string | null
    } = $props()


    function switchTab(event: MouseEvent, href: string): void
    {
        event.preventDefault()
        nav.goTo(href, true)
    }
</script>



<tab-bar>
    {#each tabs as tab (tab.label)}
        <a 
            href         = { tab.href }
            class:active = { current_tab_id === tab.id }
            aria-current = { current_tab_id === tab.id ? "page" : undefined }
            onclick      = { (event) => switchTab(event, tab.href) }
        >
            { tab.label }
        </a>
    {/each}

    {#if current_tab_id !== null}
        <tab-indicator></tab-indicator>
    {/if}
</tab-bar>



<style>
    tab-bar 
    {
        display: flex;
        flex-direction: row;
        position: relative;
        padding: 0 8px;
        flex: 1;
        border-bottom: 1px solid var(--text-placeholder);
    }

    a 
    {
        flex: 1;
        text-align: center;
        padding: 12px 16px;
        text-decoration: none;
        color: var(--text);
        font-weight: 500;

        &.active
        {
            anchor-name: --active;
        }
    }

    tab-indicator 
    {
        height: 2px;
        border-radius: 4px 4px 0 0;
        background-color: var(--text);

        position: absolute;
        position-anchor: --active;
        bottom: anchor(bottom);
        left: anchor(left);
        right: anchor(right);
        transition: all 0.2s ease;
        will-change: left, right; /* remove motion artifacts by promote the element to its own layer before the transition starts, 
                                     so it composites cleanly without touching the pixels behind it. */
    }

    @supports not (anchor-name: --anchor)
    {
        tab-indicator
        {
            display: none;
        }

        a.active
        {
            box-shadow: inset 0 -2px 0 var(--text);
        }
    }

    @media (prefers-reduced-motion: reduce)
    {
        tab-indicator
        {
            transition: none;
        }
    }
</style>