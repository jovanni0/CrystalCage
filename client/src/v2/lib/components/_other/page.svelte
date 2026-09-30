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
        overflow: hidden;
        background-color: var(--bg);
    }

    layout
    {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        overscroll-behavior: none;
    }
</style>