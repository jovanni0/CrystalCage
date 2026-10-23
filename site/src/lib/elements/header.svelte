<script lang="ts">
    import type { Component } from "svelte";
    import type { NavLink }   from "#lib/content/site-config.ts";
    import      { page }      from "$app/state";
    import      SanctumLogo   from "#lib/components/icons/sanctum-logo.svelte";
    import      Home          from "#lib/components/icons/home.svelte";
    import      Smartphone    from "#lib/components/icons/smartphone.svelte";
    import      ServerStack   from "#lib/components/icons/server-stack.svelte";
    import      IconButton    from "#lib/components/buttons/icon-button.svelte";
    import      Sun           from "#lib/components/icons/sun.svelte";
    import      Signpost      from "#lib/components/icons/signpost.svelte";
    import      Mail          from "#lib/components/icons/mail.svelte";
    import      ButtonNavbar  from "#lib/components/buttons/button-navbar.svelte";



    let {
        nav_items
    }: {
        nav_items: NavLink[]
    } = $props()


    const NAV_ICONS: Record<string, Component> =
    {
        home: Home,
        phone: Smartphone,
        server: ServerStack,
        signpost: Signpost,
        mail: Mail,
    }


    function isActiveLink(href: string): boolean
    {
        if (href === "/")
        {
            return page.url.pathname === "/"
        }

        return page.url.pathname.startsWith(href)
    }
</script>



<header>
    <brand-wrapper>
        <a
            class = "logo"
            href  = "/"
        >
            <SanctumLogo />
    
            <p class = "brand">Sanctum</p>
        </a>
    </brand-wrapper>

    <nav
        aria-label = "Main"
    >
        {#each nav_items as item (item.href)}
            {@const NavIcon = NAV_ICONS[item.icon]}
            <ButtonNavbar
                label        = { item.label }
                href         = { item.href }
                is_current   = { isActiveLink(item.href) }
                is_collapsed = { true }
            >
                <NavIcon />
            </ButtonNavbar>
        {/each}
    </nav>

    <tools-wrapper>
        <IconButton
            label = "switch theme"
            shape = "square"
        >
            <Sun />
        </IconButton>
    </tools-wrapper>
</header>



<style>
    header
    {
        position: sticky;
        top: 0;
        z-index: 10;
        background-color: var(--bg);

        display: grid;
        grid-template-columns: 1fr auto 1fr;
        align-items: center;
        padding: 0 16px 0 32px;

        border-bottom: 1px solid var(--text-placeholder);

        brand-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: flex-start;

            a.logo
            {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 12px;
    
                font-family: Lora;
                font-size: var(--text-display-sm);
                font-weight: 600;
    
                color: var(--text-brand-secondary);
                fill: var(--bg-brand-secondary);
    
                text-decoration: none;

                & > :global(svg)
                {
                    width: 48px;
                    height: 48px;
                }
            }
        }

        nav
        {
            display: flex;
            flex-direction: row;
        }

        tools-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: flex-end;
        }

        @media (width <= 1350px)
        {
            brand-wrapper > a.logo > p.brand
            {
                display: none;
            }
        }

        @media (width <= 1000px)
        {
            padding: 0 0 0 16px;

            nav
            {
                --nav-item-layout: compressed;
            }

            brand-wrapper > a.logo
            {
                p.brand
                {
                    display: block;
                    font-size: var(--text-heading-md);
                }

                & > :global(svg)
                {
                    width: 32px;
                    height: 32px;
                }
            }
        }

        @media (width <= 770px)
        {
            brand-wrapper > a.logo > p.brand
            {
                display: none;
            }
        }

        @media (width <= 560px)
        {
            grid-template-columns: 1fr 1fr;
            nav
            {
                display: none;
            }
        }
    }
</style>