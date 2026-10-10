<script lang="ts">
    import type { Snippet }       from "svelte";
    import type { NavbarTabs }    from "@v2/lib/components/bars/navbar.svelte";
    import      { PageSlots }     from "@v2/lib/utils/page-slots.svelte";
    import      { page }          from "$app/state";
    import      Page              from "@v2/lib/components/_other/page.svelte"
    import      Navbar            from "@v2/lib/components/bars/navbar.svelte";



    let { children }: { children?: Snippet } = $props()

    const slots = PageSlots.providePageSlots()

    let current_page: NavbarTabs = $derived(tabOf(page.url.pathname))


    /*
    * The first path segment names the tab; the library lives at "/".
    */
    function tabOf(pathname: string): NavbarTabs
    {
        const first_segment = pathname.split("/")[1] ?? ""

        if (first_segment === "authors" || first_segment === "read-history" || first_segment === "settings")
        {
            return first_segment
        }

        return "library"
    }
</script>



<Page>
    {#snippet topbar()}
        {@render slots.topbar?.()}
    {/snippet}


    {@render children?.()}


    {#snippet navbar()}
        <Navbar selected = { current_page } />
    {/snippet}


    {#snippet sheets()}
        {@render slots.sheets?.()}
    {/snippet}
</Page>