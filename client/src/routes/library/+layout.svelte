<script lang="ts">
    import TopbarSearch from "@components/topbar-search.svelte"
    import Button       from "@components/button.svelte";
    import FAB          from "@components/FAB.svelte";
    import { library_store } from "@root/CompositionRoot";

    
    let { children } = $props()
</script>



<div class="layout-wrapper">
    <TopbarSearch />

    {#if library_store.is_library_empty}
        <div class="filler">
            <div class="empty-message">
                <span>Your library is empty.</span>
                <span>Add your first book to get started.</span>

                <Button 
                    text="Add book" 
                    href="/manage-book"
                />
            </div>
            
        </div>

    {:else}
        <main>
            {@render children()} 
        </main>

        <div class="fab">
            <FAB href="/manage-book" />
        </div>
    {/if}
</div>



<style>
    .layout-wrapper {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        position: relative;
    }

    main {
        flex: 1;
    }

    .filler {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
    }

    .empty-message {
        display: flex;
        flex-direction: column;
        align-items: center;
        color: var(--text-secondary);
        gap: 8px;
    }

    .fab {
        position: absolute;
        right: 24px;
        bottom: 24px;
        line-height: 0;
    }
</style>