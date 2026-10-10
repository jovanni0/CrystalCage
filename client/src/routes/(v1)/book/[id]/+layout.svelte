<script lang="ts">
    import type { TopbarTab }           from "@root/types/TopbarTab";
    import type { BookStore }           from "@root/stores/BookStore.svelte";
    import      TopbarBackTabbed        from "@root/components/topbars/TopbarBackTabbed.svelte";
    import      Pencil                  from "@root/icons/Pencil.svelte";
    import      { page }                from "$app/state";
    import DropdownMenu from "@root/components/dropdown-menu.svelte";
    import { nav } from "@root/CompositionRoot.js";
    import EllipsisVertical from "@v2/lib/components/icons/ellipsis-vertical.svelte";
    import ConfirmDialog from "@root/components/confirm-dialog.svelte";

    
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

    let is_delte_popup_open = $state(false)
    let is_deleting = $state(false)


    async function deleteBook()
    {
        is_deleting = true

        const success = await book_store.delete()

        if (success)
        {
            is_deleting = false
            nav.goBack()
            return
        }

        is_deleting = false
    }
</script>



<TopbarBackTabbed 
    title = { book_store.title }
    tabs  = { tabs }
>
    {#snippet right()}
        <DropdownMenu
            label = "Book options"
            items = {[
                { label: "Edit",   onSelect: () => nav.goTo(edit_href) },
                { label: "Delete", onSelect: () => is_delte_popup_open = true, is_destructive: true },
            ]}
        >
            {#snippet icon()}
                <EllipsisVertical />
            {/snippet}
        </DropdownMenu>
    {/snippet}
</TopbarBackTabbed>

<main>
    {@render children()} 
</main>

<ConfirmDialog
    bind:is_open = { is_delte_popup_open }
    title        = "Delete this book?"
    message      = "“{ book_store.title }” and all its covers, editions and reads will be permanently removed."
    is_busy      = { is_deleting }
    onConfirm    = { deleteBook }
/>



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