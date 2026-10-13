<script lang="ts">
    import type { Snippet } from "svelte";

    let {
        children
    }: {
        children?: Snippet
    } = $props()
</script>



<cover-tile>
    {@render children?.()}
</cover-tile>



<style>
    cover-tile
    {
        display: block;
        width: 100%;
        aspect-ratio: 2/3;
        overflow: hidden;

        border: 1px solid var(--text-placeholder);
        border-radius: 8px;
        box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.2);
    }
</style>