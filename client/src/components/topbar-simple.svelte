<script lang="ts">
    import type { Snippet } from "svelte"

    let { 
        left = undefined, 
        right = undefined, 
        title = ""
    } : {
        left?: Snippet
        right?: Snippet
        title?: string
    } = $props();
</script>



<div class="simple-topbar-base">
    <div class="content">
        {#if left}
            {@render left()}
        {:else}
            <div class="button-placeholder"></div>
        {/if}
        
        <span>{title}</span>
        
        {#if right}
            {@render right()}
        {:else}
            <div class="button-placeholder"></div>
        {/if}
    </div>
</div>
    


<style>
    .simple-topbar-base {
        background-color: var(--base);

        position: sticky;
        top: 0;
        container-type: scroll-state;
        z-index: 1;
    }

    @container scroll-state(stuck: top) {
        .content {
            border-bottom: 1px solid var(--dividers) !important;
        }
    }

    .content {
        display: flex;
        flex-direction: row;
        gap: 4px;
        align-items: center;
        padding: 12px 16px;
        padding-top: calc(max(12px, env(safe-area-inset-top)));
        border-bottom: 1px solid var(--base);
        transition: border-color 0.25s ease;
    }

    .button-placeholder {
        min-width: 48px;
        min-height: 48px;
    }

    span {
        flex: 1;
        text-align: center;
        font-size: larger;
        font-weight: 500;
    }
</style>