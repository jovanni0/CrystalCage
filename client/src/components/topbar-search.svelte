<script lang="ts">
    import TopbarShell from "./topbar-shell.svelte"
    import Searchbar from "./searchbar.svelte"
    import TabBar from "./tab-bar.svelte";
</script>


<TopbarShell>
    {#snippet left()}
        <img 
            class="avatar"
            src="/avatar.jpg" 
            alt="avatar"
        >
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
                { label: "Gallery", href: "/library"},
                { label: "by Author", href: "/library/by-author"},
                { label: "by Series", href: "/library/by-series"},
            ]}
        />
    {/snippet}
</TopbarShell>



<style>
    .avatar {
        aspect-ratio: 1/1;
        width: 48px;
        border-radius: 100px;
    }

    .placeholder {
        width: 48px;
    }
</style>