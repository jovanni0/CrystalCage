<script lang="ts">
    import type { TagGroup }        from "../stores/book-management.store.svelte";
    import      SearchBarWithCreate from "../components/_other/search-bar-with-create.svelte";
    import      Button              from "../components/buttons/button.svelte";
    import      PillButton          from "../components/buttons/pill-button.svelte";
    import      TagCategoryListItem from "../components/list-items/tag-category-list-item.svelte";
    import      BottomSheet         from "../components/_other/bottom-sheet.svelte";



    let {
        is_open                   = $bindable(),
        search                    = $bindable(),
        selected_ids              = $bindable(),
        search_placeholder,
        empty_placeholder,
        empty_search_palceholder,
        show_create               = false,
        tag_groups,
        is_empty                  = false,
        onCreate,
    }: {
        is_open?:                 boolean
        search?:                  string
        selected_ids?:            string[]
        search_placeholder:       string
        empty_placeholder:        string
        empty_search_palceholder: string
        show_create?:             boolean
        tag_groups:               TagGroup[]
        is_empty?:                boolean
        onCreate?:                () => void
    } = $props()


    
    let draft_selected_ids = $derived.by( () =>
    {
        is_open
        
        return selected_ids ? [...selected_ids] : []
    })
    

    const has_changed = $derived(selected_ids?.length !== draft_selected_ids.length || draft_selected_ids.some(id => !selected_ids?.includes(id)))


    function toggleId(id: string)
    {
        if (draft_selected_ids.includes(id))
        {
            draft_selected_ids = draft_selected_ids.filter(it => it !== id)
        }
        else
        {
            draft_selected_ids = [...draft_selected_ids, id]
        }
    }


    $effect( () =>
    {
        if (is_open === false)
        {
            search = ""
        }
    })
</script>



<BottomSheet
    bind:is_open
    policy = "fit-expand"
>
    {#snippet header()}
        <SearchBarWithCreate 
            bind:value  = { search }
            placeholder = { search_placeholder }
            show_create = { show_create }
            onCreate    = { onCreate }
        />  
    {/snippet}

    <category-list
        class:empty = { is_empty || tag_groups.length === 0 }
    >
        {#if is_empty}
            <p class = "empty-message">{ empty_placeholder }</p>
        {:else if tag_groups.length === 0}
            <p class = "empty-search-message">{ empty_search_palceholder }</p>
        {:else}
            {#each tag_groups as tag_group (tag_group.id)}
                <TagCategoryListItem
                    label = { tag_group.name }
                >
                    {#each tag_group.tags as tag (tag.id)}
                        <PillButton
                            is_selected = { draft_selected_ids.includes(tag.id) }
                            onClick     = { () => toggleId(tag.id) }
                        >
                            { tag.name }
                        </PillButton>
                    {/each}
                </TagCategoryListItem>
            {/each}
        {/if}
    </category-list>

    {#snippet footer()}
        <Button 
            onclick = { () => { selected_ids = draft_selected_ids; is_open = false } }
            state   = { !has_changed ? "disabled" : "default" }
        >
            {#if draft_selected_ids.length === 0 && selected_ids?.length !== 0}
                Clear all tags
            {:else}
                Assign { draft_selected_ids.length } tags
            {/if}
        </Button>
    {/snippet}
</BottomSheet>



<style>
    category-list
    {
        display: flex;
        flex-direction: column;
        gap: 32px;

        overflow-y: auto;
        min-height: 128px;
        height: 100%;

        &.empty
        {
            justify-content: center;
            align-content: center;
        }
    }

    .empty-message,
    .empty-search-message
    {
        color: var(--text-placeholder);
        text-align: center;
    }
</style>