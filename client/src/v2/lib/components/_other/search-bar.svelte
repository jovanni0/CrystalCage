<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
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
        <IconButton
            shape = "square"
            label = "search"
        >
            <Search />
        </IconButton>
    {:else}
        <IconButton
            shape = "square"
            label = "clear-search"
            onclick = { () => value = "" }
        >
            <Ex />
        </IconButton>
    {/if}
</search-bar>



<style>
    search-bar
    {
        display: flex;
        flex-direction: row;
        gap: 12;
        padding: 0 8px 0 24px;
        border-radius: 999px;
        border: 1px solid var(--text-placeholder);
        background-color: rgba(0, 0, 0, 0.1);

        &:has(input:focus-visible)
        {
            border: 2px solid var(--text-brand);
            margin: -1px;
        }
    }

    input
    {
        all: unset;
        -webkit-appearance: none;
        appearance: none;

        min-height: 48px;
        min-width: 0;
        flex: 1;

        font-family: Manrope;
        font-size: var(--text);
        background-color: transparent;

        &:focus
        {
            outline: none;
        }

        &::-webkit-search-cancel-button 
        {
            -webkit-appearance: none;
            appearance: none;
        }
    }
</style>