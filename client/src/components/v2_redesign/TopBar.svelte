<script lang="ts">
    import { nav }     from "@root/CompositionRoot";
    import ChevronLeft from "@root/icons/chevron-left.svelte";

    let {
        title,
        type,
        fallback_url,
    }: {
        title: string
        type:  "back"
        fallback_url?: string
    } = $props()
</script>



<div class = "top-bar">
    {#if type === "back"}
        <button 
            class   = "back-button"
            onclick = { () => nav.goBack(fallback_url) }
        >
            <ChevronLeft />
        </button>
    {/if}
    <h1>{ title }</h1>
</div>



<style>
    .top-bar
    {
        position: relative;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
    }

    .back-button
    {
        position: absolute;
        left: 6px;
        top: 50%;
        transform: translateY(-50%);

        width: 48px;
        height: 48px;

        display: flex;
        align-items: center;
        justify-content: center;

        border-radius: 8px;
    }

    h1
    {
        font-size: var(--text-heading-sm);
        font-weight: 500;
    }
</style>