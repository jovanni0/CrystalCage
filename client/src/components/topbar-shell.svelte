<script lang="ts">
    import type { Snippet } from "svelte"

    let { 
        left = undefined, 
        center = undefined, 
        right = undefined, 
        bottom= undefined 
    } : {
        left?: Snippet
        center?: Snippet
        right?: Snippet
        bottom?: Snippet
    } = $props();
</script>


<div class="top-bar-base">
    <div class="main-row">
        <div class="left">
            {@render left?.()}
        </div>
        <div class="center">
            {@render center?.()}
        </div>
        <div class="right">
            {@render right?.()}
        </div>
    </div>

    <div class="bottom">
        {@render bottom?.()}
    </div>
</div>



<style>
    .top-bar-base {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    .main-row {
        display: flex;
        flex-direction: row;
        gap: 8px;
        padding: 0 16px;
        padding-top: calc(max(8px, env(safe-area-inset-top)));
    }

    .left, .center, .right, .bottom {
        display: flex;
        flex-direction: row;
        align-items: flex-start;
    }

    .center {
        align-items: center;
        flex: 1;
        min-width: 0;
    }

    .bottom {
        flex: 1;
    }
</style>