<script lang="ts">
    import type { Snippet } from "svelte";
    import "./category-list-item-styles.css"



    let {
        title,
        children,
    }: {
        title: string
        children?: Snippet
    } = $props()
</script>



<category-list-item
    class = "category-list-item"
>
    <p class = "category">
        { title.toLocaleUpperCase() }
    </p>

    {@render children?.()}
</category-list-item>