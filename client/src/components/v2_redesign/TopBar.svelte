<script lang="ts">
    import ChevronLeft from "@root/icons/chevron-left.svelte";
    import { goBack } from "@root/utils/go-back";

    let {
        title,
        type
    }: {
        title: string
        type:  "back"
    } = $props()
</script>



<div class = "top-bar">
    {#if type === "back"}
        <button 
            class   = "back-button"
            onclick = { goBack }
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