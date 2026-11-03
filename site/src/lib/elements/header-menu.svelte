<script lang="ts">
    import type { Component } from "svelte";
    import type { NavLink }   from "#lib/content/site-config.ts";
    import      { afterNavigate } from "$app/navigation";
    import      IconButton    from "#lib/components/buttons/icon-button.svelte";
    import      Ex            from "#lib/components/icons/ex.svelte";
    import      Sparkles      from "#lib/components/icons/sparkles.svelte";
    import      Sun           from "#lib/components/icons/sun.svelte";



    let {
        is_open = $bindable(false),
        nav_items,
        nav_icons,
        is_current,
        show_ai,
        onAskAi,
        onToggleTheme,
    }: {
        is_open?:      boolean
        nav_items:     NavLink[]
        nav_icons:     Record<string, Component>
        is_current:    (href: string) => boolean
        show_ai:       boolean
        onAskAi:       () => void
        onToggleTheme: () => void
    } = $props()


    let dialog: HTMLDialogElement


    $effect(() =>
    {
        if (is_open && !dialog.open)
        {
            dialog.showModal()
        }

        if (!is_open && dialog.open)
        {
            dialog.close()
        }
    })


    // following a link closes the menu
    afterNavigate(() =>
    {
        is_open = false
    })


    // the content fills the whole sheet, so a click on the dialog itself is a click on the backdrop
    function closeOnBackdrop(event: MouseEvent)
    {
        if (event.target === dialog)
        {
            is_open = false
        }
    }


    function askAi()
    {
        is_open = false
        onAskAi()
    }
</script>



<dialog
    bind:this  = { dialog }
    class      = "header-menu"
    aria-label = "Menu"
    closedby   = "any"
    onclose    = { () => is_open = false }
    onclick    = { closeOnBackdrop }
>
    <menu-content>
        <menu-top>
            <p class = "menu-title">Menu</p>

            <IconButton
                label   = "close menu"
                shape   = "square"
                onclick = { () => is_open = false }
            >
                <Ex />
            </IconButton>
        </menu-top>

        <nav aria-label = "Main">
            <ul>
                {#each nav_items as item (item.href)}
                    {@const NavIcon = nav_icons[item.icon]}
                    <li>
                        <a
                            class        = "menu-link"
                            href         = { item.href }
                            aria-current = { is_current(item.href) ? "page" : undefined }
                        >
                            <NavIcon />
                            <span>{ item.label }</span>
                        </a>
                    </li>
                {/each}
            </ul>
        </nav>

        <hr>

        <menu-actions>
            {#if show_ai}
                <button
                    type    = "button"
                    class   = "menu-link"
                    onclick = { askAi }
                >
                    <Sparkles />
                    <span>Ask AI</span>
                </button>
            {/if}

            <button
                type    = "button"
                class   = "menu-link"
                onclick = { onToggleTheme }
            >
                <Sun />
                <span>Switch theme</span>
            </button>
        </menu-actions>
    </menu-content>
</dialog>



<style>
    /* while the menu is open, the page behind it does not scroll */
    :global(html:has(dialog.header-menu[open]))
    {
        overflow: hidden;
    }


    dialog.header-menu
    {
        box-sizing: border-box;
        width: min(320px, 85vw);
        max-width: none;
        height: 100dvh;
        max-height: none;
        margin: 0 0 0 auto;
        padding: 0;

        border: none;
        border-left: 1px solid var(--border-neutral-subtle);
        background-color: var(--bg-surface-raised);
        color: var(--text-neutral-default);

        translate: 100% 0;
        transition:
            translate 200ms ease-out,
            overlay   200ms allow-discrete,
            display   200ms allow-discrete;

        &[open]
        {
            translate: 0 0;

            @starting-style
            {
                translate: 100% 0;
            }
        }

        &::backdrop
        {
            background-color: rgb(0 0 0 / 0);
            transition:
                background-color 200ms,
                overlay          200ms allow-discrete,
                display          200ms allow-discrete;
        }

        &[open]::backdrop
        {
            background-color: rgb(0 0 0 / 0.4);

            @starting-style
            {
                background-color: rgb(0 0 0 / 0);
            }
        }

        @media (prefers-reduced-motion: reduce)
        {
            &,
            &::backdrop
            {
                transition-duration: 1ms;
            }
        }
    }


    menu-content
    {
        display: flex;
        flex-direction: column;
        gap: 8px;

        box-sizing: border-box;
        height: 100%;
        padding: 8px 12px 24px 12px;
    }


    menu-top
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;

        padding-left: 16px;
    }


    p.menu-title
    {
        font-size: var(--text-heading-sm);
        font-weight: 600;
    }


    ul,
    menu-actions
    {
        display: flex;
        flex-direction: column;
        gap: 4px;

        margin: 0;
        padding: 0;
        list-style: none;
    }


    hr
    {
        width: 100%;
        margin: 8px 0;

        border: none;
        border-top: 1px solid var(--border-neutral-subtle);
    }


    .menu-link
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 16px;

        box-sizing: border-box;
        width: 100%;
        min-height: 48px;
        padding: 0 16px;

        border-radius: 999px;

        color: inherit;
        font-weight: 500;
        text-decoration: none;

        &:hover
        {
            background-color: var(--bg-overlay-subtle);
        }

        &:focus-visible
        {
            outline: 2px solid var(--text-brand-default);
            outline-offset: -2px;
        }

        &[aria-current="page"]
        {
            background-color: var(--bg-brand-subtle);
            color: var(--text-brand-default);
            font-weight: 600;
        }
    }
</style>
