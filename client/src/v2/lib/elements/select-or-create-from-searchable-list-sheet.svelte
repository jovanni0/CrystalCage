<script lang="ts">
    import SearchBar        from "@v2/lib/components/_other/search-bar.svelte";
    import Button           from "@v2/lib/components/buttons/button.svelte";
    import CreateListItem   from "@v2/lib/components/list-items/create-list-item.svelte";
    import RadioListItem    from "@v2/lib/components/list-items/radio-list-item.svelte";
    import Sheetv2          from "./sheetv2.svelte";



    let {
        is_open     = $bindable(),
        search      = $bindable(),
        search_placeholder,
        items,
        selected_id = $bindable(),
        create_label,
        button_label,
        onCreate,
    }: {
        is_open?:      boolean
        search?:       string
        search_placeholder?: string
        items:         { id: string, label: string }[],
        selected_id?:  string | null
        create_label?: string | null,
        button_label:  string
        onCreate?:     () => void
    } = $props()


    let draft_id = $derived.by( () =>
    {
        is_open

        return selected_id ?? null
    })


    $effect( () =>
    {
        if (is_open === false)
        {
            search = ""
        }
    })
</script>



<Sheetv2 bind:is_open>
    {#snippet header()}
        <SearchBar 
            bind:value = { search }
            placeholder = { search_placeholder }
        />
    {/snippet}

    <author-list 
        class = "scrollbar-thin"
    >
        {#if create_label}
            <CreateListItem 
                label = { create_label }
                onclick = { onCreate }
            />
        {/if}

        {#each items as item (item.id)}
            <RadioListItem 
                label = { item.label }
                is_selected = { draft_id === item.id }
                onSelect = { () => draft_id = item.id }
            />
        {/each}
    </author-list>
    
    {#snippet footer()}
        <Button 
            class = "primary-button"
            onclick = { () => selected_id = draft_id }
            state = { !draft_id || draft_id === selected_id ? "disabled" : "default" }
        >
            { button_label }
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