<script lang="ts">
    import type { AuthorStore }  from "@root/stores/AuthorStore.svelte.js";
    import type { TopbarTab }    from "@root/types/TopbarTab.js";
    import      FAB              from "@components/FAB.svelte";
    import      TopbarBackTabbed from "@root/components/topbars/TopbarBackTabbed.svelte";

    
    let { data, children } = $props()

    const { author_store }: { author_store: AuthorStore } = $derived(data)
    const tabs: TopbarTab[] = $derived([
        { label: "Gallery", href: `/author/${author_store.author_id}` },
        { label: "by Series", href: `/author/${author_store.author_id}/by-series` },
    ])
</script>



<TopbarBackTabbed 
    title = { author_store.author_name }
    tabs  = { tabs }
/>

<main>
    {@render children()} 
</main>

<div class="fab">
    <FAB href="/manage-book" />
</div>



<style>
    main 
    {
        flex: 1;
    }

    .fab 
    {
        position: absolute;
        right: 24px;
        bottom: 24px;
        line-height: 0;
    }
</style>