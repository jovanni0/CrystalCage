<script lang="ts">
    import type { Component } from "svelte";

    let {
        Icon = undefined
    } : {
        Icon?: Component<any>
    } = $props()
</script>


<button class="topbar-button">
    {#if Icon}
        <Icon />
    {/if}
</button>



<style>
    .topbar-button {
        min-width: 48px;
        aspect-ratio: 1/1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }
</style>