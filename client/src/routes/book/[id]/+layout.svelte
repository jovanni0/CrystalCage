<script lang="ts">
    import type { TopbarTab }           from "@root/types/TopbarTab";
    import type { BookStore }           from "@root/stores/BookStore.svelte";
    import      TopbarBackTabbed        from "@root/components/topbars/TopbarBackTabbed.svelte";
    import      Pencil                  from "@root/icons/Pencil.svelte";
    import      { page }                from "$app/state";

    
    let { data, children } = $props()

    const { 
        book_store, 
        id 
    } : {
        book_store: BookStore,
        id: string
    } = $derived(data)

    // the edit button opens the manager that matches the open tab
    const edit_href: string = $derived.by( () =>
    {
        const route_id = page.route.id ?? ""

        if (route_id.endsWith("/covers"))
        {
            return `/manage-covers/${id}`
        }

        if (route_id.endsWith("/reads"))
        {
            return `/manage-editions/${id}`
        }

        return `/add-book/${id}`
    })

    const tabs: TopbarTab[] = $derived([
        { label: "Details", href: `/book/${book_store.book_id}` },
        { label: "Covers", href: `/book/${book_store.book_id}/covers` },
        { label: "Reads", href: `/book/${book_store.book_id}/reads` },
    ])
</script>



<TopbarBackTabbed 
    title = { book_store.title }
    tabs  = { tabs }
>
    {#snippet right()}
        <a href = { edit_href }>
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