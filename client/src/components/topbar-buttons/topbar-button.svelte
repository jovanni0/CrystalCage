<script lang="ts">
    import type { Snippet } from "svelte";

    let { 
        content = undefined,
        onClick = undefined
    } : { 
        content?: Snippet 
        onClick?: () => void
    } = $props()
</script>



{#if content}
    <button onclick={onClick}>
        {@render content()}
    </button>
{:else}
    <div class="button-placeholder"></div>
{/if}



<style>
    button,
    .button-placeholder {
        width: 48px;
        height: 48px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }
</style>