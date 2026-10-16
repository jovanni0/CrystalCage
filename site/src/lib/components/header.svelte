<script lang="ts">
    import SanctumLogo from "./icons/sanctum-logo.svelte";
    import Home from "./icons/home.svelte";
    import Smartphone from "./icons/smartphone.svelte";
    import ServerStack from "./icons/server-stack.svelte";
    import { page } from "$app/state";
    import Phone from "./icons/phone.svelte";
    import IconButton from "./buttons/icon-button.svelte";
    import Sun from "./icons/sun.svelte";
    import type { Component } from "svelte";
    import Signpost from "./icons/signpost.svelte";
    import Mail from "./icons/mail.svelte";
    import type { NavLink } from "#lib/content/site-config.ts";



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
            <SanctumLogo 
                width  = { 48 }
                height = { 48 }
            />
    
            <p class = "brand">Sanctum</p>
        </a>
    </brand-wrapper>

    <nav
        aria-label = "Main"
    >
        {#each nav_items as item (item.href)}
            {@const NavIcon = NAV_ICONS[item.icon]}
            <a
                href  = { item.href }
                class = "nav-link"
                aria-current = { isActiveLink(item.href) ? "page" : undefined }
            >
                <content-wrapper>
                    <NavIcon />
                    <p class = "label">{ item.label }</p>
                </content-wrapper>
            </a>
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
            }
        }

        nav
        {
            display: flex;
            flex-direction: row;

            a.nav-link
            {
                margin: 16px;
                text-decoration: none;
                color: var(--text);
                
                content-wrapper
                {
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                    padding: 12px 24px;
                    gap: 8px;

                    font-weight: 600;

                    border-radius: 999px;
                }

                &[aria-current="page"]
                {
                    content-wrapper
                    {
                        color: var(--text-brand);
                        background-color: var(--bg-brand-pale);
                    }
                }

                &:hover
                {
                    color: var(--text-brand);
                }
            }
        }

        tools-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: flex-end;
        }
    }
</style>