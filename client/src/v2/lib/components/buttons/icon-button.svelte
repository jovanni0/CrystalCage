<script lang="ts">
    import type { HTMLButtonAttributes } from "svelte/elements";
    import      { ripple_effect } from "./ripple-effect";
    import      "./ripple-effect.css"



    let {
        shape     = "round",
        state     = "default",
        children,
        class: className,
        label,
        onclick,
        ...rest
    }: HTMLButtonAttributes & {
        shape?: "round" | "square"
        state?: "default" | "disabled"
        label:  string
    } = $props()
</script>



<button
    disabled   = { state === "disabled" }
    class      = {[ "ripple-container", shape, state, className ]}
    aria-label = { label }
    onclick    = { onclick}

    {@attach ripple_effect}
    { ...rest }
>
    {@render children?.()}
</button>



<style>
    button
    {
        display: flex;
        align-items: center;
        justify-content: center;
        position: relative;
        user-select: none;
        cursor: pointer;
        width: 54px;
        height: 54px;
    }

    button.round
    {
        border-radius: 999px;
        border: 1px solid var(--text-brand);
        color: var(--text-brand);
        background-color: var(--bg-brand-pale);

        &.disabled
        {
            cursor: auto;
            background-color: transparent;
            color: var(--text-brand-disabled);
            border-color: var(--text-brand-disabled);
        }
    }


    button.square
    {
        border-radius: 8px;
        color: var(--text);

        &.disabled
        {
            color: var(--text-placeholder);
            cursor: auto;
        }
    }
</style>