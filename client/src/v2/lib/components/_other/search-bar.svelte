<script lang="ts">
    import Ex from "../icons/ex.svelte";
    import Search from "../icons/search.svelte";

    let {
        placeholder = "",
        value = $bindable(),
    }: {
        placeholder?: string
        value?: string
    } = $props()
</script>



<search-bar>
    <input 
        type = "search"
        placeholder = { placeholder }
        bind:value
    >

    {#if !value}
        <Search />
    {:else}
        <Ex />
    {/if}
</search-bar>



<style>
    search-bar
    {
        display: flex;
        flex-direction: row;
        gap: 12;
        padding: 4px 8px 4px 16px;
        border-radius: 999px;
        border: 1px solid var(--text-placeholder);
    }

    input
    {
        min-width: 0;
        flex: 1;
    }
</style>