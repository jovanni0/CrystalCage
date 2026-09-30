<script lang="ts">
    import type { HTMLAnchorAttributes } from "svelte/elements";
    import { ripple_effect } from "./ripple-effect";
    import      "./button-styles.css"
    import      "./ripple-effect.css"



    let {
        size    = "fat",
        variant = "primary",
        state   = "default",
        children,
        class: className,
        href,
        ...rest
    }: HTMLAnchorAttributes & {
        size?:    "fat" | "slim"
        variant?: "primary" | "ghost"
        state?:   "default" | "disabled"
    } = $props()
</script>



<a
    aria-disabled  = { state === "disabled" }
    class          = {[ "button-mimic", "ripple-container", variant, state, size, className ]}
    href           = { href }

    {@attach ripple_effect}
    { ...rest }
>
    {@render children?.()}
</a>