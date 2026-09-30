<script lang="ts">
    import BottomSheet      from "@v2/lib/components/_other/bottom-sheet.svelte";
    import SearchBar        from "@v2/lib/components/_other/search-bar.svelte";
    import Button           from "@v2/lib/components/buttons/button.svelte";
    import CreateListItem   from "@v2/lib/components/list-items/create-list-item.svelte";
    import RadioListItem    from "@v2/lib/components/list-items/radio-list-item.svelte";
    import Sheetv2          from "./sheetv2.svelte";


    let {
        is_open = $bindable(),
        search = $bindable(),
    }: {
        is_open?: boolean
        search?: string
    } = $props()



    let selected_author_id = $state("")

    const authors = [
        { id: "1", name: "Bird, Morton"},
        { id: "2", name: "Hogan, Tammie"},
        { id: "3", name: "Howe, Carlo"},
        { id: "4", name: "Rose, Darla"},
        { id: "5", name: "Wilson, Eula"},
        { id: "6", name: "Yates, Chandra"},
    ] as const
</script>



<Sheetv2 bind:is_open>
    {#snippet header()}
        <SearchBar bind:value = { search }/>
    {/snippet}

    <author-list 
        class = "scrollbar-thin"
    >
        {#if search}
            <CreateListItem 
                label = { search }
            />
        {/if}

        {#each authors as author (author.id)}
        <RadioListItem 
                label = { author.name }
                is_selected = { selected_author_id === author.id }
                onSelect = { () => selected_author_id = author.id }
            />
        {/each}
    </author-list>
    
    {#snippet footer()}
        <Button class = "primary-button">
            Set Author
        </Button>
    {/snippet}
</Sheetv2>



<style>
    author-list
    {
        display: flex;
        flex-direction: column;
        min-width: 0;
        flex: 1;
        overflow-y: auto;
    }
</style>