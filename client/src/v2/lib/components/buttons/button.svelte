<script lang="ts">
    import type { HTMLButtonAttributes } from "svelte/elements";
    import      { ripple_effect } from "./ripple-effect";
    import      Spinner                  from "../_other/loading-spinner.svelte";
    import      "./button-styles.css"
    import      "./ripple-effect.css"



    let {
        size    = "fat",
        variant = "primary",
        state   = "default",
        children,
        class: className,
        onclick,
        ...rest
    }: HTMLButtonAttributes & {
        size?:    "fat" | "slim"
        variant?: "primary" | "ghost"
        state?:   "default" | "loading" | "disabled"
    } = $props()
</script>



<button
    disabled       = { state === "disabled" }
    aria-busy      = { state === "loading" }
    class          = {[ "button-mimic", "ripple-container", variant, state, size, className ]}
    onclick        = { onclick}

    {@attach ripple_effect}
    { ...rest }
>
    <content class:loading = { state === "loading" }>
        {@render children?.()}
    </content>

    {#if state === "loading"}
        <spinner-wrapper>
            <Spinner />
        </spinner-wrapper>
    {/if}
</button>