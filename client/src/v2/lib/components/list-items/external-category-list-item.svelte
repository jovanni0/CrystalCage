<script lang="ts">
    import type { HTMLButtonAttributes } from "svelte/elements";
    import ChevronRight from "../icons/chevron-right.svelte";
    import "./category-list-item-styles.css"
    import { ripple_effect } from "../buttons/ripple-effect";
    import "../buttons/ripple-effect.css"



    let {
        title,
        children,
        class: className,
        ...rest
    }: HTMLButtonAttributes & {
        title: string
    } = $props()
</script>



<button
    id = "category-{title}"
    class = {[ "category-list-item", "ripple-container", className ]}
    {@attach ripple_effect}
    { ...rest }
>
    <top-wrapper>
        <label 
            class = "category"
            for = "category-{title}"
        >
            { title.toLocaleUpperCase() }
        </label>
        <ChevronRight />
    </top-wrapper>

    {@render children?.()}
</button>



<style>
    button
    {
        position: relative
    }
</style>