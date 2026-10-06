<script lang="ts">
    import type { Snippet } from "svelte";
    import { ripple_effect } from "./ripple-effect";
    import "./ripple-effect.css"

    let {
        class: className,
        children,
        is_selected = false,
        onClick,
    }: {
        class?:    string
        children?: Snippet
        is_selected: boolean
        onClick?: () => void
    } = $props()
</script>



<button
    class          = {[ "pill-button", className ]}
    class:selected = { is_selected }
    onclick        = { onClick }
    {@attach ripple_effect}
>
    {@render children?.()}
</button>



<style>
    button
    {
        padding: 6px 12px;
        border-radius: 999px;
        border: 1px solid var(--text-muted);

        &.selected
        {
            color: var(--text-brand);
            border-color: var(--text-brand);
            background-color: var(--bg-brand-pale);
        }
    }
</style>