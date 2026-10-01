<script lang="ts">
    import type { HTMLAnchorAttributes } from "svelte/elements";
    import ChevronRight from "../icons/chevron-right.svelte";
    import "./category-list-item-styles.css"
    import { ripple_effect } from "../buttons/ripple-effect";
    import "../buttons/ripple-effect.css"



    let {
        title,
        children,
        class: className,
        href,
        ...rest
    }: HTMLAnchorAttributes & {
        title: string
    } = $props()
</script>



<a
    id = "category-{title}"
    class = {[ "category-list-item", "ripple-container", className ]}
    href = { href }
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
</a>



<style>
    a
    {
        position: relative;
        cursor: pointer;
    }
</style>