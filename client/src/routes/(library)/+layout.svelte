<script lang="ts">
    import type { LibraryViews }  from "@root/types/LibraryViews";
    import      { library_store } from "@root/CompositionRoot";
    import      FAB               from "@components/FAB.svelte";
    import      Searchbar         from "@root/components/searchbar.svelte";
    import      TabBar            from "@root/components/TabBar.svelte";
    import      TopbarShell       from "@root/components/topbar-shell.svelte";
    import { goto } from "$app/navigation";

    
    let { children } = $props()
</script>



<main>
    <TopbarShell>
        {#snippet left()}
            <button class = "avatar" onclick={ () => goto("/generate-code")}>
                <img 
                    src="/avatar.jpg" 
                    alt="avatar"
                >
            </button>
        {/snippet}

        {#snippet center()}
            <Searchbar />
        {/snippet}

        {#snippet right()}
            <div class="placeholder"></div>
        {/snippet}

        {#snippet bottom()}
            <TabBar 
                tabs = {[
                    { label: "Gallery",   value: "GALLERY"},
                    { label: "by Author", value: "AUTHOR"},
                    { label: "by Series", value: "SERIES"},
                ]}
                selected = { library_store.view }
                onselect = { (value) => library_store.view = value as LibraryViews }
            />
        {/snippet}
    </TopbarShell>
    
    {@render children()}
    
    <FAB href = "/manage-book" />
</main>



<style>
    main
    {
        position: relative;
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    :global(.fab)
    {
        position: absolute;
        right: 24px;
        bottom: 24px;
        line-height: 0;
    }

    :global(.top-bar-base)
    {
        position: sticky;
        top: 0;
        left: 0;
        background-color: var(--base);
    }

    .avatar {
        display: flex;
        align-items: center;
        justify-content: center;

        img
        {
            aspect-ratio: 1/1;
            width: 48px;
            border-radius: 100px;
        }
    }

    .placeholder {
        width: 48px;
    }
</style>