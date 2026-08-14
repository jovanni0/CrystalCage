<script lang="ts">
    import type { TopbarTab }    from "@root/types/TopbarTab.js";
    import      TopbarBackTabbed from "@root/components/topbars/TopbarBackTabbed.svelte";
    import      Pencil           from "@root/icons/Pencil.svelte";

    
    let { data, children } = $props()

    const { book_store } = $derived(data)
    const tabs: TopbarTab[] = $derived([
        { label: "Details", href: `/book/${book_store.book_id}` },
        { label: "Covers", href: `/book/${book_store.book_id}/covers` },
        { label: "Reads", href: `/book/${book_store.book_id}/reads` },
    ])
</script>



<TopbarBackTabbed 
    title = { book_store.author_name }
    tabs  = { tabs }
>
    {#snippet right()}
        <a href = { "" }>
            <Pencil />
        </a>
    {/snippet}
</TopbarBackTabbed>

<main>
    {@render children()} 
</main>



<style>
    a
    {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    main 
    {
        flex: 1;
    }
</style>