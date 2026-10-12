<script lang="ts">
    import type { Snippet } from "svelte";
    import      { ripple_effect, rippleOn } from "./ripple-effect";
    import      "./ripple-effect.css"



    let {
        label,
        href,
        is_selected = false,
        children,
    }: {
        label:        string
        href:         string
        is_selected?: boolean
        children:     Snippet
    } = $props()
</script>



<a
    href  = { href }
    {@attach rippleOn("icon-wrapper")}
>
    <icon-wrapper
        class:selected = { is_selected }
        class          = "ripple-container"
    >
        {@render children?.()}
    </icon-wrapper>

    <p class = "button-label">{ label }</p>
</a>



<style>
    a
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        user-select: none;
        cursor: pointer;
        overflow: hidden;
        
        border-radius: 8px;

        padding: 6px;
        gap: 4px;
    }

    icon-wrapper
    {
        display: flex;
        align-items: center;
        justify-content: center;

        width: 56px;
        height: 32px;
        border-radius: 999px;

        background-color: transparent;

        &.selected
        {
            background-color: var(--bg-brand-disabled);
        }
    }

    p.button-label
    {
        font-size: var(--text-caption);
        text-align: center;
    }
</style>