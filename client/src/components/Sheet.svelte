<script lang="ts">
    import type { Snippet } from "svelte";


    let {
        children,
    }: {
        children?: Snippet
    } = $props();


    let dialog_el: HTMLDialogElement;

    export function open()  { dialog_el.showModal(); }
    export function close() { dialog_el.close(); }
</script>



<dialog 
    bind:this={dialog_el} 
    class="sheet"
>
    <div class="handle"></div>

    {@render children?.()}
</dialog>



<style>
    .sheet 
    {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        top: auto;
        width: 100%;
        max-width: none;
        margin: 0;
        border: none;
        border-radius: 20px 20px 0 0;
        padding: 24px 16px;
        background: var(--base);
        box-sizing: border-box;
    }

    .sheet[open] 
    {
        display: flex;
        flex-direction: column;
    }

    .sheet::backdrop 
    { 
        background: rgba(0, 0, 0, 0.4);
    }

    .handle 
    {
        width: 40px;
        height: 4px;
        border-radius: 2px;
        background: var(--dividers);
        margin: 0 auto 20px;
    }
</style>
