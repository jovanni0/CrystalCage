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
    import      Search        from "#lib/components/icons/search.svelte";
    import      Menu          from "#lib/components/icons/menu.svelte";
    import      Sparkles      from "#lib/components/icons/sparkles.svelte";
    import      HeaderMenu    from "#lib/elements/header-menu.svelte";
    import      { chat_store }          from "#lib/stores/chat-store.svelte.ts";
    import      { search_state }        from "#lib/states/search-state.svelte.ts";
    import      { PUBLIC_CHAT_ENABLED } from "$app/env/public";



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


    let is_menu_open: boolean = $state(false)

    // the chat only exists on docs pages; below 1000px its floating button is hidden, so the header offers it
    const can_ask_ai: boolean = $derived(PUBLIC_CHAT_ENABLED && (page.route.id?.startsWith("/docs") ?? false))


    function openChat()
    {
        chat_store.is_open = true
    }


    function isActiveLink(href: string): boolean
    {
        if (href === "/")
        {
            return page.url.pathname === "/"
        }

        return page.url.pathname.startsWith(href)
    }


    function toggleTheme()
    {
        const root = document.documentElement
        const is_dark = root.classList.contains("dark") || (!root.classList.contains("light") && matchMedia("(prefers-color-scheme: dark)").matches)
        const next = is_dark ? "light" : "dark"

        root.classList.remove("light", "dark")
        root.classList.add(next)

        try
        {
            localStorage.setItem("theme", next)
        }
        catch
        {

        }
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
        <button
            type       = "button"
            class      = "search-trigger"
            aria-label = "Search"
            onclick    = { () => search_state.open() }
        >
            <span class = "search-hint">Search…</span>
            <Search />
        </button>

        {#if can_ask_ai}
            <IconButton
                label   = "Ask AI"
                shape   = "square"
                class   = "ask-ai"
                onclick = { openChat }
            >
                <Sparkles />
            </IconButton>
        {/if}

        <IconButton
            label   = "switch theme"
            shape   = "square"
            class   = "theme-toggle"
            onclick = { toggleTheme }
        >
            <Sun />
        </IconButton>

        <IconButton
            label         = "open menu"
            shape         = "square"
            class         = "menu-toggle"
            aria-haspopup = "dialog"
            aria-expanded = { is_menu_open }
            onclick       = { () => is_menu_open = true }
        >
            <Menu />
        </IconButton>
    </tools-wrapper>
</header>

<HeaderMenu
    bind:is_open  = { is_menu_open }
    nav_items     = { nav_items }
    nav_icons     = { NAV_ICONS }
    is_current    = { isActiveLink }
    show_ai       = { can_ask_ai }
    onAskAi       = { openChat }
    onToggleTheme = { toggleTheme }
/>



<style>
    header
    {
        position: sticky;
        top: 0;
        z-index: 10;
        background-color: var(--bg-surface-page);

        display: grid;
        grid-template-columns: 1fr auto 1fr;
        align-items: center;
        padding: 0 16px 0 32px;

        border-bottom: 1px solid var(--border-neutral-subtle);

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
    
                color: var(--text-accent-default);
                fill: var(--text-accent-default);
    
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
            align-items: center;
            justify-content: flex-end;
            gap: 4px;

            :global(button.ask-ai),
            :global(button.menu-toggle)
            {
                display: none;
            }
        }

        button.search-trigger
        {
            display: flex;
            flex-direction: row;
            align-items: center;
            justify-content: space-between;
            gap: 12px;

            box-sizing: border-box;
            width: 220px;
            height: 44px;
            padding: 0 12px 0 20px;

            border: 1px solid var(--border-neutral-default);
            border-radius: 999px;

            color: var(--text-neutral-subtle);

            &:hover
            {
                background-color: var(--bg-overlay-subtle);
            }

            &:focus-visible
            {
                outline: 2px solid var(--text-brand-default);
                outline-offset: 2px;
            }
        }

        @media (width <= 1400px)
        {
            brand-wrapper > a.logo > p.brand
            {
                display: none;
            }

            button.search-trigger
            {
                justify-content: center;

                width: 48px;
                height: 48px;
                padding: 0;

                border: none;
                border-radius: 8px;

                color: var(--text-neutral-default);

                span.search-hint
                {
                    display: none;
                }
            }
        }

        @media (width <= 1050px)
        {
            padding: 0 0 0 16px;

            tools-wrapper :global(button.ask-ai)
            {
                display: flex;
            }

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

        @media (width <= 800px)
        {
            grid-template-columns: auto 1fr auto;

            nav
            {
                justify-content: center;
            }

            brand-wrapper > a.logo > p.brand
            {
                display: none;
            }
        }

        @media (width <= 700px)
        {
            grid-template-columns: 1fr 1fr;
            nav
            {
                display: none;
            }

            /* the hidden navigation, the theme switch and the chat move into the menu */
            tools-wrapper
            {
                :global(button.ask-ai),
                :global(button.theme-toggle)
                {
                    display: none;
                }

                :global(button.menu-toggle)
                {
                    display: flex;
                }
            }
        }
    }
</style>