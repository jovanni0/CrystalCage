<script lang="ts">
    import Button from "@root/components/button.svelte";
    import NavigationListItem from "@root/components/NavigationListItem.svelte";

    const tags = [
        { label: "Action", usage: 0 },
        { label: "Adventure", usage: 4 },
        { label: "Alien", usage: 4 },
        { label: "Epic", usage: 3 },
        { label: "Fiction", usage: 8 },
        { label: "Futuristic", usage: 2 },
    ].sort((a, b) => b.usage - a.usage)
</script>



<ul class="content">
        {#each tags as entry (entry.label)}
            <NavigationListItem 
                label = { entry.label }
                hint  = { `${entry.usage} books` }
            />
        {/each}
</ul>



<style>
    .content 
    {
        display: flex;
        flex-direction: column;
        padding: 16px;
        margin: 0;
    }
</style>