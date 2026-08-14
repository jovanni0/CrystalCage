<script lang="ts">
    import type { TopbarTab } from "@root/types/TopbarTab";
    import      TopbarShell   from "../topbar-shell.svelte";
    import      { goBack }    from "@root/utils/go-back";
    import      ChevronLeft   from "@root/icons/chevron-left.svelte";
    import      TabBar        from "../tab-bar.svelte";

    let {
        title,
        tabs = [],
    } : {
        title: string,
        tabs: TopbarTab[]
    } = $props()
</script>



<TopbarShell>
    {#snippet left()}
        <button onclick = { goBack }>
            <ChevronLeft />
        </button>
    {/snippet}

    {#snippet center()}
        <span class = "title">{ title }</span>
    {/snippet}

    {#snippet right()}
        <div class = "placeholder"></div>
    {/snippet}

    {#snippet bottom()}
        <TabBar tabs = { tabs }/>
    {/snippet}
</TopbarShell>



<style>
    button, .placeholder
    {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .title
    {
        font-size: large;
        font-weight: 500;
        text-align: center;
        flex: 1;
    }
</style>