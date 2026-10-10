<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
    import Plus from "../icons/plus.svelte";
    import Search from "../icons/search.svelte";


    let {
        onAddBook
    }: {
        onAddBook: () => void
    } = $props()
</script>



<topbar-brand-search>
    <p class = "brand-name">Sanctum</p>

    <buttons-wrapper>
        <IconButton
            label = "search"
            shape = "square"
        >
            <Search />
        </IconButton>

        <IconButton
            label   = "search"
            shape   = "square"
            onclick = { onAddBook }
        >
            <Plus />
        </IconButton>
    </buttons-wrapper>
</topbar-brand-search>



<style>
    topbar-brand-search
    {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        padding: 0 4px 8px 20px;
        user-select: none;
    }

    p.brand-name
    {
        font-family: Lora;
        font-size: var(--text-heading-md);
        font-weight: 500;
        color: var(--text-brand-secondary);
    }

    buttons-wrapper
    {
        display: flex;
        flex-direction: row;
    }
</style>