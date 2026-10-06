<script lang="ts">
    import type { Snippet } from "svelte";
    import "./category-list-item-styles.css"



    let {
        title,
        children,
        class: className,
    }: {
        title: string
        children?: Snippet
        class?: string
    } = $props()
</script>



<category-list-item
    class = {[ "category-list-item", className ]}
>
    <p class = "category">
        { title.toLocaleUpperCase() }
    </p>

    {@render children?.()}
</category-list-item>