<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
    import Ex from "../icons/ex.svelte";
    import Plus from "../icons/plus.svelte";
    import Search from "../icons/search.svelte";

    let {
        placeholder = "",
        value       = $bindable(),
        show_create = false,
        onCreate,
    }: {
        placeholder?: string
        value?:       string
        show_create?: boolean
        onCreate?:    () => void
    } = $props()
</script>



<search-bar-with-create>
    <search-field>
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
    </search-field>
    
    <IconButton
        label = "create"
        onclick = { onCreate }
        class = {[ "create", show_create && "open" ]}
    >
        <Plus />
    </IconButton>
</search-bar-with-create>



<style>
    search-bar-with-create
    {
        display: flex;
        flex-direction: row;
        gap: 8px;
    }

    search-field
    {
        display: flex;
        flex-direction: row;
        flex: 1;
        gap: 12;
        padding: 0 8px 0 24px;
        border-radius: 999px;
        border: 1px solid var(--text-placeholder);
        background-color: rgba(0, 0, 0, 0.1);
        transition: width 200ms cubic-bezier(0.05, 0.7, 0.1, 1);

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

    :global(button.create)
    {
        width: 0;
        margin-right: 0;
        opacity: 0;
        overflow: hidden;

        transition: width 200ms cubic-bezier(0.05, 0.7, 0.1, 1);

        &.open
        {
            width: 54px;
            margin-right: 4px;
            opacity: 1;
        }
    }
</style>