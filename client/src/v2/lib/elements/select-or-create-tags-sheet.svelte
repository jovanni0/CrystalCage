<script lang="ts">
    import SearchBarWithCreate from "../components/_other/search-bar-with-create.svelte";
    import Button from "../components/buttons/button.svelte";
    import PillButton from "../components/buttons/pill-button.svelte";
    import TagCategoryListItem from "../components/list-items/tag-category-list-item.svelte";
    import type { TagResponse } from "../dtos/tag.dto";
    import Sheetv2 from "./sheetv2.svelte";



    let {
        is_open      = $bindable(),
        search       = $bindable(),
        selected_ids = $bindable(),
        show_create = false,
        tag_groups,
        onCreate,
    }: {
        is_open?:      boolean
        search?:       string
        selected_ids?: string[]
        show_create?:  boolean
        tag_groups:    { id: string, name: string, tags: TagResponse[] }[]
        onCreate?:     () => void
    } = $props()


    
    let draft_selected_ids = $derived.by( () =>
    {
        is_open
        
        return selected_ids ? [...selected_ids] : []
    })
    
    const had_tags = $derived((selected_ids?.length ?? 0) > 0)
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
</script>



<Sheetv2
    bind:is_open
>
    {#snippet header()}
        <SearchBarWithCreate 
            bind:value  = { search }
            placeholder = "Type a tag to search or add..."
            show_create = { show_create }
            onCreate    = { onCreate }
        />  
    {/snippet}

    <category-list>
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
</Sheetv2>



<style>
    category-list
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
    }
</style>