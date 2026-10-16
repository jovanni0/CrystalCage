<script lang="ts">
    import { page } from "$app/state"
    import "#lib/styles/tokens.css";
    import "#lib/styles/fonts.css";
    import "#lib/styles/base.css";
    import "#lib/styles/prose.css";
    import Header from "#lib/components/header.svelte";
    import Footer from "#lib/components/footer.svelte";

    let { data, children } = $props()



    function isActiveLink(href: string): boolean
    {
        if (href === "/")
        {
            return page.url.pathname === "/"
        }

        return page.url.pathname.startsWith(href)
    }
</script>



<Header 
    nav_items = { data.site.nav } 
/>

<page-content>
    {@render children()}
</page-content>

<Footer />