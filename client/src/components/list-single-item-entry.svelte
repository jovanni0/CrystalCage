<script lang="ts">
    import type { Snippet } from "svelte";
    import ChevronRight from "./icons/chevron-right.svelte";

    let {
        key = "KEY",
        placeholder = "None set",
        href,
        hasContent = false,
        children
    } : {
        key?: string
        placeholder?: string
        href?: string
        hasContent?: boolean
        children?: Snippet
    } = $props()
</script>



{#if href}
    <a 
        href={href}
        class="entry"
    >
        <div class="top">
            <span class="key">{key}</span>
            <ChevronRight />
        </div>

        {#if !hasContent}
            <span class="none-set">{placeholder}</span>
        {:else}
            <div class="content-wrapper">
                {@render children?.()}
            </div>
        {/if}
    </a>
{:else}
    <button class="entry">
        <div class="top">
            <span class="key">{key}</span>
            <ChevronRight />
        </div>

        <span class="none-set">{placeholder}</span>
    </button>
{/if}




<style>
    .entry {
        display: flex;
        flex-direction: column;
        gap: 8px;
        padding: 12px 0;
    }

    .top {
        display: flex;
        flex-direction: row;
        gap: 8px;
    }

    .key {
        flex: 1;
        padding: 0 8px;
        font-weight: 700;
        color: var(--text-secondary);
        letter-spacing: 0.05em;
    }

    .none-set {
        flex: 1;
        font-style: italic;
        color: var(--text-secondary);
        padding: 0 8px;
    }

    .content-wrapper {
        flex: 1;
        padding: 0 8px;
    }

    :global(.content-wrapper p) {
        margin: 1.25rem 0;
        text-align: justify;
        letter-spacing: 0.03em;
    }

    :global(.content-wrapper p:first-child) {
        margin-top: 0;
    }

    :global(.content-wrapper p:last-child) {
        margin-bottom: 0;
    }
</style>