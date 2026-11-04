<script lang="ts">
    import type { Snippet } from "svelte";

    let {
        key,
        children
    }: {
        key:       string
        children?: Snippet
    } = $props()
</script>



<key-value-list-item>
    <p class = "key">{ key }</p>
    
    {@render children?.()}
</key-value-list-item>



<style>
    key-value-list-item
    {
        display: flex;
        flex-direction: row;
        gap: 16px;
        justify-content: space-between;
        padding: 12px 8px;
        border-bottom: 1px solid var(--text-placeholder);
    }

    p.key
    {
        flex-shrink: 0;
    }
</style>