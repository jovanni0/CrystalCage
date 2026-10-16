<script lang="ts">
    import { page } from "$app/state"
    import "#lib/styles/tokens.css";
    import "#lib/styles/fonts.css";
    import "#lib/styles/base.css";
    import "#lib/styles/prose.css";

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



<site-header>
    <a class = "brand" href = "/">{ data.site.name }</a>

    <nav>
        {#each data.site.nav as link (link.href)}
            <a href = { link.href } aria-current = { isActiveLink(link.href) ? "page" : undefined }>{ link.label }</a>
        {/each}
    </nav>
</site-header>

<page-content>
    {@render children()}
</page-content>

<site-footer>
    <p>{ data.site.footer.note }</p>
</site-footer>