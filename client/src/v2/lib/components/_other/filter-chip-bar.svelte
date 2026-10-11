<script lang="ts">
    import { ripple_effect } from "../buttons/ripple-effect"
    import "../buttons/ripple-effect.css"

    let {
        options,
        value = $bindable(),
        name,
        label,
    }: {
        options: { id: string, label: string }[]
        value:   string
        name:    string
        label:   string
    } = $props()



    function revealSelected(node: HTMLElement): void
    {
        node.scrollIntoView({ inline: "nearest", block: "nearest" })
    }
</script>



<filter-chip-bar
    role       = "radiogroup"
    aria-label = { label }
>
    {#each options as option (option.id)}
        <label
            class          = "ripple-container"
            class:selected = { option.id === value }
            {@attach ripple_effect}
            {@attach option.id === value ? revealSelected : undefined}
        >
            <input
                type       = "radio"
                name       = { name }
                value      = { option.id }
                bind:group = { value }
            />
            { option.label }
        </label>
    {/each}
</filter-chip-bar>



<style>
    filter-chip-bar
    {
        display: flex;
        gap: 8px;
        overflow-x: auto;
        scrollbar-width: none;

        mask-image: linear-gradient(to right, black calc(100% - 32px), transparent);
    }

    label
    {
        position: relative;
        flex: none;
        overflow: hidden;

        padding: 8px 16px;
        border-radius: 999px;
        border: 1px solid var(--text-muted);

        white-space: nowrap;
        font-size: var(--text-body-sm);
        color: var(--text);
        cursor: pointer;
        -webkit-tap-highlight-color: transparent;

        &.selected
        {
            border-color: var(--bg-brand);
            background-color: var(--bg-brand);
            color: var(--text-inverted);
        }

        &:has(input:focus-visible)
        {
            outline: 2px solid var(--bg-brand);
            outline-offset: 2px;
        }
    }

    input
    {
        position: absolute;
        opacity: 0;
        width: 1px;
        height: 1px;
        pointer-events: none;
    }
</style>