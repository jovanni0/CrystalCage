<script lang="ts">
    import type { DisplayLabel }    from "../stores/book-management.store.svelte";
    import      Button              from "@v2/lib/components/buttons/button.svelte";
    import      RadioListItem       from "@v2/lib/components/list-items/radio-list-item.svelte";
    import      BottomSheet         from "../components/_other/bottom-sheet.svelte";
    import      IconButton          from "../components/buttons/icon-button.svelte";
    import      Ex                  from "../components/icons/ex.svelte";
    import      SearchBarWithCreate from "../components/_other/search-bar-with-create.svelte";



    let {
        is_open                   = $bindable(),
        search                    = $bindable(),
        selected_id               = $bindable(),
        search_placeholder,
        empty_placeholder,
        empty_search_palceholder,
        items,
        create_label,
        button_label,
        is_resetable              = true,
        is_empty                  = false,
        onCreate,
    }: {
        is_open?:                  boolean
        search?:                   string
        search_placeholder?:       string
        empty_placeholder?:        string
        empty_search_palceholder?: string
        items:                     DisplayLabel[],
        selected_id?:              string | null
        create_label?:             string | null,
        button_label:              string
        is_resetable?:             boolean
        is_empty?:                 boolean
        onCreate?:                 () => void
    } = $props()


    let draft_id = $derived.by( () =>
    {
        is_open

        return selected_id ?? null
    })

    const is_selection_removeable = $derived(selected_id && draft_id === selected_id)
    const show_create = $derived(create_label != null && create_label !== "")

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
        <search-wrapper>
            <SearchBarWithCreate 
                bind:value  = { search }
                placeholder = { search_placeholder }
                show_create = { show_create }
                onCreate    = { onCreate }
            />
    
            <p
                class = "create-message"
                class:invisible = { !show_create }
            >
                Will be saved as: { create_label }
            </p>
        </search-wrapper>
    {/snippet}

    <author-list
        class:empty = { is_empty || items.length === 0 }
    >
        {#if is_empty}
            <p class = "empty-message">{ empty_placeholder }</p>
        {:else if items.length === 0}
            <p class = "empty-search-message">{ empty_search_palceholder }</p>
        {:else}
            {#each items as item (item.id)}
                <RadioListItem 
                    label       = { item.label }
                    is_selected = { draft_id === item.id }
                    onSelect    = { () => draft_id = item.id }
                />
            {/each}
        {/if}
    </author-list>
    
    {#snippet footer()}
        <button-wrapper class:content-empty = { is_empty }>
            {#if is_resetable}
                <IconButton 
                    class   = { [ "clear", is_selection_removeable && "open" ] }
                    shape   = "round"
                    label   = "back"
                    onclick = { () => selected_id = null }
                >
                    <Ex />
                </IconButton>
            {/if}

            <Button 
                class   = "primary-button"
                onclick = { () => selected_id = draft_id }
                state   = { !draft_id || draft_id === selected_id ? "disabled" : "default" }
                style   = "flex: 1;"
            >
                { button_label }
            </Button>
        </button-wrapper>
    {/snippet}
</BottomSheet>



<style>
    search-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    .create-message
    {
        font-size: var(--text-caption);
        padding: 0 24px;
        margin-bottom: -16px;

        &.invisible
        {
            visibility: hidden;
        }
    }

    author-list
    {
        display: flex;
        flex-direction: column;
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

    button-wrapper
    {
        display: flex;
        flex-direction: row;
        padding-top: 16px;

        &.content-empty
        {
            padding-top: 0;
        }
    }


    :global(button.clear)
    {
        width: 0;
        margin-right: 0;
        opacity: 0;
        overflow: hidden;

        transition: width 200ms cubic-bezier(0.05, 0.7, 0.1, 1);

        &.open
        {
            width: 54px;
            margin-right: 4px;
            opacity: 1;
        }
    }
</style>