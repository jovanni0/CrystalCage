<script lang="ts">
    import type { Snippet } from "svelte";

    let {
        label,
        children,
    }: {
        label?:    string
        children?: Snippet
    } = $props()
</script>



<tag-category-list-item>
    {#if label}
        <p class = "category-label">{ label.toUpperCase() }</p>
    {/if}

    <tag-list>
        {@render children?.()}
    </tag-list>
</tag-category-list-item>



<style>
    tag-category-list-item
    {
        position: relative;
        display: flex;
        flex-direction: column;
        gap: 8px;
        padding-left: 12px;

        &::before
        {
            content: "";
            position: absolute;
            inset: 0 auto 0 0;
            width: 4px;
            border-radius: 999px;
            background-color: var(--text-placeholder);
        }
    }

    tag-list
    {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 4px;
    }

    .category-label
    {
        letter-spacing: 10%;
        color: var(--text-brand-secondary);
    }
</style>