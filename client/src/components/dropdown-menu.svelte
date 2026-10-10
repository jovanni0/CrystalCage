<script lang="ts" module>
    export type DropdownMenuItem = {
        label:           string
        is_destructive?: boolean
        onSelect:        () => void
    }
</script>



<script lang="ts">
    import type { Snippet }           from "svelte"
    import      { generateEphemeralId } from "@root/utils/IdGenerator"
    import IconButton from "@v2/lib/components/buttons/icon-button.svelte";


    let {
        label,
        icon,
        items,
    }: {
        label: string
        icon:  Snippet
        items: DropdownMenuItem[]
    } = $props()

    const menu_id = `menu-${generateEphemeralId()}`

    let menu: HTMLElement



    function select(item: DropdownMenuItem): void
    {
        menu.hidePopover()
        item.onSelect()
    }
</script>



<IconButton
    { label }
    popovertarget = { menu_id }
    style         = "anchor-name: --{ menu_id }"
>
    {@render icon()}
</IconButton>

<dropdown-menu
    bind:this = { menu }
    id        = { menu_id }
    popover   = "auto"
    role      = "menu"
    style     = "position-anchor: --{ menu_id }"
>
    {#each items as item (item.label)}
        <button
            role              = "menuitem"
            class:destructive = { item.is_destructive }
            onclick           = { () => select(item) }
        >
            { item.label }
        </button>
    {/each}
</dropdown-menu>



<style>
    dropdown-menu
    {
        inset: auto;
        margin: 4px 0;
        position-area: bottom span-left;
        position-try-fallbacks: flip-block;

        min-width: 160px;
        padding: 4px;
        border: none;
        border-radius: 12px;
        background-color: var(--base);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);

        &:popover-open
        {
            display: flex;
            flex-direction: column;
        }
    }

    button
    {
        padding: 12px 16px;
        border: none;
        border-radius: 8px;
        background: none;

        font: inherit;
        font-size: var(--text-body);
        text-align: start;
        color: var(--text);

        &:active
        {
            background-color: var(--bg-inverted-muted);
        }

        &.destructive
        {
            color: var(--text-error);
        }
    }
</style>