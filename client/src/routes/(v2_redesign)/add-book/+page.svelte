<script lang="ts">
    import ExternalCategoryListItem from "@v2/lib/components/list-items/external-category-list-item.svelte";
    import DateListItem             from "@v2/lib/components/list-items/date-list-item.svelte";
    import ExternalPickerListItem   from "@v2/lib/components/list-items/external-picker-list-item.svelte";
    import CategoryListItem         from "@v2/lib/components/list-items/category-list-item.svelte";
    import InputField               from "@v2/lib/components/_other/input-field.svelte";
    import BottomSheet              from "@v2/lib/components/_other/bottom-sheet.svelte";
    import Page                     from "@v2/lib/components/_other/page.svelte"
    import IconButton               from "@v2/lib/components/buttons/icon-button.svelte";
    import Ex                       from "@v2/lib/components/icons/ex.svelte";
    import Button                   from "@v2/lib/components/buttons/button.svelte";
    import SelectAuthorSheet        from "@v2/lib/elements/select-author-sheet.svelte";


    let author = $state("")
    let author_sheet = $state(true)

    let universe = $state("")
    let universe_sheet = $state(false)

    let series = $state("")
    let series_sheet = $state(false)

    let volume = $state("")
    let volume_sheet = $state(false)

    let word_count = $state("")
    let word_count_sheet = $state(false)

    let release_date = $state("")
    let tags: string[] = $state([])
    let blurb = $state("")
    let thoughts = $state("")
</script>



<!-- ------------------------------------ -->



<Page class = "page-content">
    {#snippet topbar()}
        <tb-cancel-action>
            <IconButton 
                shape = "square"

                label = "back"
                onclick = { () => {} }
            >
                <Ex />
            </IconButton>

            <p>New Book</p>

            <Button
                size = "slim"
                onclick = { () => {} }
            >
                Save
            </Button>
        </tb-cancel-action>
    {/snippet}


    <wrapper>
        <InputField 
            label = "Title"
        />

        <list>
            <ExternalPickerListItem 
                key = "Author"
                value = { author }
                is_required = { true }
                onclick = { () => author_sheet = true }
            />

            <ExternalPickerListItem 
                key = "Universe"
                value = { universe }
                disabled_message = { !author ? "Needs author" : undefined }
                onclick = { () => universe_sheet = true }
            />

            <ExternalPickerListItem 
                key = "Series"
                value = { series }
                disabled_message = { !author ? "Needs author" : undefined }
                onclick = { () => series_sheet = true }
            />

            <ExternalPickerListItem 
                key = "Volume"
                value = { volume }
                disabled_message = { !series ? "Needs series" : undefined }
                is_required = { series ? true : false }
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
                value = { word_count }
                onclick = { () => word_count_sheet = true }
            />

            <DateListItem 
                key = "Release date"
                bind:ISO_date = { release_date }
            />
        </list>
    </CategoryListItem>


    <ExternalCategoryListItem
        title = "tags"
    >
        {#if tags.length === 0}
            <p class = "category-text-content">No tags yet. Choose from your list or create a new one.</p>
        {:else}
            <list>
            </list>
        {/if}
    </ExternalCategoryListItem> 


    <ExternalCategoryListItem
        title = "blurb"
    >
        <p class = "category-text-content">
            { blurb ? blurb : "No blurb yet. Add a summary to describe what this book is about." }
        </p>
    </ExternalCategoryListItem> 


    <ExternalCategoryListItem
        title = "thoughts"
    >
        <p class = "category-text-content">
            { thoughts ? thoughts : "No thoughts yet. Tap to write whatever you'd like." }
        </p>
    </ExternalCategoryListItem>


    {#snippet sheets()}
        <SelectAuthorSheet bind:is_open = { author_sheet} />
        <BottomSheet bind:is_open = { universe_sheet }></BottomSheet>
        <BottomSheet bind:is_open = { series_sheet }></BottomSheet>
        <BottomSheet bind:is_open = { volume_sheet }></BottomSheet>
        <BottomSheet bind:is_open = { word_count_sheet }></BottomSheet>
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

    .category-text-content
    {
        padding: 0 8px 0 8px;
        color: var(--text-muted);
        text-justify: distribute;
    }

    tb-cancel-action
    {
        position: relative;
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        padding: 8px 16px;

        & > p
        {
            font-size: var(--text-heading-sm);
            font-weight: 500;
            position: absolute;
            inset: 50% 50% auto auto;
            transform: translate(50%, -50%);
        }
    }
</style>