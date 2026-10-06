<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
    import Search from "../icons/search.svelte";


</script>



<topbar-brand-search>
    <p class = "brand-name">Sanctum</p>
    <IconButton
        label = "search"
        shape = "square"
    >
        <Search />
    </IconButton>
</topbar-brand-search>



<style>
    topbar-brand-search
    {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        padding: 16px 24px 0 24px;
    }

    .brand-name
    {
        font-family: Lora;
        font-size: var(--text-heading-md);
        font-weight: 500;
        color: var(--text-brand-secondary);
    }
</style>