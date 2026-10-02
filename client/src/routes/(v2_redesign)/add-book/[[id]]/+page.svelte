<script lang="ts">
    import ExternalCategoryListItem from "@v2/lib/components/list-items/category-button-list-item.svelte";
    import DateListItem             from "@v2/lib/components/list-items/date-list-item.svelte";
    import ExternalPickerListItem   from "@v2/lib/components/list-items/external-picker-list-item.svelte";
    import CategoryListItem         from "@v2/lib/components/list-items/category-list-item.svelte";
    import InputField               from "@v2/lib/components/_other/input-field.svelte";
    import Page                     from "@v2/lib/components/_other/page.svelte"
    import { CreateBookStore }      from "@v2/lib/stores/book-management.store.svelte";
    import { goBack } from "@root/utils/go-back.js";
    import SelectOrCreateFromSearchableListSheet from "@v2/lib/elements/select-or-create-from-searchable-list-sheet.svelte";
    import SelectVolumeNumberSheet from "@v2/lib/elements/select-volume-number-sheet.svelte";
    import SetWordCountSheet from "@v2/lib/elements/set-word-count-sheet.svelte";
    import SelectOrCreateTagsSheet from "@v2/lib/elements/select-or-create-tags-sheet.svelte";
    import GenericPill from "@v2/lib/components/_other/generic-pill.svelte";
    import CategoryLinkListItem from "@v2/lib/components/list-items/category-link-list-item.svelte";
    import TopbarCancelAction from "@v2/lib/components/topbars/topbar-cancel-action.svelte";
    import { renderMarkdown } from "@v2/lib/utils/markdown.js";
    import { tag_repository } from "@root/CompositionRoot.js";


    let { data } = $props()
    const { 
        book_store,
        id
    } : { 
        book_store: CreateBookStore
        id:         string | null 
    } = $derived(data)

    let author_sheet     = $state(false)
    let universe_sheet   = $state(false)
    let series_sheet     = $state(false)
    let volume_sheet     = $state(false)
    let word_count_sheet = $state(false)
    let tags_sheet       = $state(false)

    let error_message = $state<string|null>(null)
    const current_path = $derived(`/add-book${id ? `/${id}` : ""}`)

    const rendered_thoughts:  string = $derived(book_store.thoughts ? renderMarkdown(book_store.thoughts) : "")
    const rendered_blurb:     string = $derived(book_store.blurb ? renderMarkdown(book_store.blurb) : "")


    async function save()
    {
        const result = await book_store.save()

        if (result)
        {
            goBack()

            return
        }

        error_message = "An error occurred while trying to save the book"
    }


    async function deleteBook()
    {
        const is_deleted = await book_store.delete()

        if (is_deleted)
        {
            goBack()

            return
        }

        error_message = "An error occurred while trying to delete the book"
    }
</script>



<!-- ------------------------------------ -->



<Page class = "page-content">
    {#snippet topbar()}
        <TopbarCancelAction 
            title              = "Add Book" 
            is_action_disabled = { !book_store.can_submit }
            onCancel           = { goBack }
            onAction           = { save }
        />
    {/snippet}


    <wrapper>
        <InputField 
            label        = "Title"
            bind:value   = { book_store.title }
            is_mandatory = { true }
        />

        <list>
            <ExternalPickerListItem 
                key = "Author"
                label = { book_store.author_name ?? "" }
                is_required = { true }
                onclick = { () => author_sheet = true }
            />

            <ExternalPickerListItem 
                key = "Universe"
                label = { book_store.universe_name ?? "" }
                disabled_message = { !book_store.is_author_selected ? "Needs author" : undefined }
                onclick = { () => universe_sheet = true }
            />

            <ExternalPickerListItem 
                key = "Series"
                label = { book_store.series_name ?? "" }
                disabled_message = { !book_store.is_author_selected ? "Needs author" : undefined }
                onclick = { () => series_sheet = true }
            />

            <ExternalPickerListItem 
                key = "Volume"
                label = { book_store.volume_number?.toString() ?? "" }
                disabled_message = { !book_store.is_series_selected ? "Needs series" : undefined }
                is_required = { book_store.is_series_selected ? true : false }
                onclick = { () => volume_sheet = true }
            />
        </list>
    </wrapper>


    <CategoryListItem 
        title = "details"
    >
        <list>
            <ExternalPickerListItem 
                key = "Word count"
                label = { book_store.word_count ? `~ ${book_store.word_count.toString()} K` : "" }
                onclick = { () => word_count_sheet = true }
            />

            <DateListItem 
                key = "Release date"
                bind:ISO_date = { book_store.launch_date }
            />
        </list>
    </CategoryListItem>


    <ExternalCategoryListItem
        title   = "tags"
        onclick = { () => tags_sheet = true }
    >
        {#if book_store.selected_tags.length === 0}
            <p class = "category-placeholder-text">No tags yet. Choose from your list or create a new one.</p>
        {:else}
            <tags-list>
                {#each book_store.selected_tags as category (category.id)}
                    {#each category.tags as tag (tag.id)}
                        <GenericPill 
                            label = { tag.name }
                        />
                    {/each}
                {/each}
            </tags-list>
        {/if}
    </ExternalCategoryListItem> 


    <CategoryLinkListItem
        title = "blurb"
        href  = "/add-book/blurb"
    >
        {#if book_store.blurb}
            {@html rendered_blurb}
        {:else}
            <p class = "category-placeholder-text">No blurb yet. Add a summary to describe what this book is about.</p>
        {/if}
    </CategoryLinkListItem> 


    <CategoryLinkListItem
        title = "thoughts"
        href  = "/add-book/thoughts"
    >
        {#if book_store.thoughts}
            {@html rendered_thoughts}
        {:else}
            <p class = "category-placeholder-text">No thoughts yet. Tap to write whatever you'd like.</p>
        {/if}
    </CategoryLinkListItem>


    {#snippet sheets()}
        <SelectOrCreateFromSearchableListSheet 
            bind:is_open      = { author_sheet} 
            bind:search       = { book_store.author_search }
            bind:selected_id  = { () => book_store.author_id ?? "", 
                                  (value: string) => { book_store.setAuthorId(value); author_sheet = false } }
            search_placeholder = "Type a name to search or add"
            items             = { book_store.author_results }
            create_label      = { book_store.author_create_label }
            button_label      = "Set author"
            onCreate          = { () => { book_store.createAndSetAuthor(); author_sheet = false } }
        />

        <SelectOrCreateFromSearchableListSheet 
            bind:is_open     = { universe_sheet } 
            bind:search      = { book_store.universe_search }
            items            = { book_store.universe_results }
            bind:selected_id = { () => book_store.universe_id ?? "", 
                                 (value: string) => { book_store.setUniverseId(value); universe_sheet = false } }
            search_placeholder = "Type a universe to search or add"
            create_label     = { book_store.universe_create_label }
            button_label     = "Set universe"
            onCreate         = { () => { book_store.createAndSetUniverse(); universe_sheet = false } }
        />

        <SelectOrCreateFromSearchableListSheet 
            bind:is_open     = { series_sheet } 
            bind:search      = { book_store.series_search }
            items            = { book_store.series_results }
            bind:selected_id = { () => book_store.series_id ?? "", 
                                 (value: string) => { book_store.setSeriesId(value); series_sheet = false } }
            search_placeholder = "Type a series to search or add"
            create_label     = { book_store.series_create_label }
            button_label     = "Set series"
            onCreate         = { () => { book_store.createAndSetSeries(); series_sheet = false } }
        />
        
        <SelectVolumeNumberSheet 
            bind:is_open = { volume_sheet }
            bind:volume  = { book_store.volume_number }
            volumes      = { book_store.other_volumes }
            next_volume  = { book_store.next_volume }
        />

        <SetWordCountSheet 
            bind:is_open   = { word_count_sheet }
            bind:value     = { book_store.word_count }
            slider_min     = { 0 }
            slider_max     = { 300 }
            wpp_coeficient = { 0.3 }
            editions       = { book_store.editions_wc }
        />

        <SelectOrCreateTagsSheet 
            bind:is_open      = { tags_sheet }
            bind:search       = { book_store.tag_search }
            bind:selected_ids = { book_store.tag_ids }
            tag_groups        = { book_store.tag_groups }
            show_create       = { book_store.should_create_tag }
            is_empty          = { tag_repository.all.length === 0 }
            onCreate          = { () => book_store.createTag() }
        />
    {/snippet}
</Page>



<!-- ------------------------------------ -->



<style>
    :global(.page-content)
    {
        display: flex;
        flex-direction: column;
        padding: 8px 16px;
        gap: 40px;
    }

    wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    list
    {
        display: flex;
        flex-direction: column;
    }

    tags-list
    {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 4px;
    }

    .category-placeholder-text
    {
        padding: 0 8px 0 8px;
        color: var(--text-muted);
        text-justify: distribute;
    }
</style>