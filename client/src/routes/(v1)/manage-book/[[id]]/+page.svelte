<script lang="ts">
    import type { BookManagementStore } from "@root/stores/BookManagementStore.svelte.js";
    import      { goBack }              from "@root/utils/go-back";
    import      { marked }              from "marked";
    import      Chip                    from "@root/components/chip.svelte";
    import      ChipFormat              from "@root/components/chip-format.svelte";
    import      ListEntry               from "@root/components/list-entry.svelte";
    import      ListEntryDate           from "@root/components/list-entry-date.svelte";
    import      ListEntryNumber         from "@root/components/list-entry-number.svelte";
    import      ListEntryText           from "@root/components/list-entry-text.svelte";
    import      ListSingleItemEntry     from "@root/components/list-single-item-entry.svelte";
    import      TopbarConfirmCancel     from "@root/components/topbar-confirm-cancel.svelte";
    import      CoverImage              from "@root/components/CoverImage.svelte";
    import      Button                  from "@root/components/button.svelte";


    let { data } = $props()
    const { 
        book_store,
        id
    } : { 
        book_store: BookManagementStore
        id:         string | undefined 
    } = $derived(data)

    let error_message = $state<string|null>(null)
    const current_path = $derived(`/manage-book${id ? `/${id}` : ""}`)

    async function save()
    {
        const result = await book_store.save()

        if (result)
        {
            goBack()
        }

        error_message = "An error occurred while trying to save the book"
    }


    async function deleteBook()
    {
        const is_deleted = await book_store.deleteBook()

        if (is_deleted)
        {
            goBack()
        }

        error_message = "An error occurred while trying to delete the book"
    }
</script>



<TopbarConfirmCancel
    title     = { book_store.is_new_book === undefined ? "" : book_store.is_new_book === true ? "Add Book" : "Edit Book"}
    onCancel  = { goBack }
    onConfirm = { save }
/>

<div class="body">
    {#if error_message !== null}
        <span class="banner">{ error_message }</span>
    {/if}

    <ListSingleItemEntry 
        key         = "COVERS"
        placeholder = "None added. Tap to modify."
        href        = { `${current_path}/covers` }
        hasContent  = { book_store.has_covers }
    >
        <div class="cover-wrapper">
            <CoverImage 
                source = { book_store.default_cover?.image_source ?? null}
                alt    = "default-cover"
                width  = "120px"
            />

            <div class="info">
                <span 
                    class       = "title"
                    class:unset = { book_store.default_cover === null }
                >
                    { book_store.default_cover?.label ?? "(No title set)" }
                </span>
                <span class="message">{ book_store.cover_message }</span>
            </div>
        </div>
    </ListSingleItemEntry>

    <div class="list">
        <span class="heading">IDENTITY</span>

        <ListEntry
            key   = "Author"
            value = { book_store.author_name ?? undefined }
            href  = { `${current_path}/author` }
        />
        <ListEntry
            key   = "Universe"
            value = { book_store.universe_name ?? undefined }
            href  = { `${current_path}/universe` }
        />
        <ListEntry
            key   = "Series"
            value = { book_store.series_name ?? undefined }
            href  = { `${current_path}/series` }
        />
        <ListEntryNumber
            key        = "Volume"
            bind:value = {
                () => book_store.volume_number ?? undefined,
                (v) => book_store.volume_number = v ?? null
            }
        />
        <ListEntryText
            key        = "Title"
            bind:value = {
                () => book_store.title ?? undefined,
                (v) => book_store.title = v ?? null
            }
        />
    </div>

    <div class="list">
        <span class="heading">DETAILS</span>

        <ListEntryDate
            key        = "Release date"
            bind:value = {
                () => book_store.launch_date?.toISOString().slice(0, 10) ?? "",
                (v) => book_store.launch_date = v ? new Date(v) : null
            }
        />

        <ListEntryNumber
            key        = "Word Count"
            bind:value = {
                () => book_store.word_count ?? undefined,
                (v) => book_store.word_count = v ?? null
            }
        />
    </div>

    <ListSingleItemEntry
        key         = "TAGS"
        placeholder = "None set. Tap to select from a preset list or customize from scratch."
        href        = { `${current_path}/tags` }
        hasContent  = { book_store.cover_count > 0 }
    >
        <div class="tags">
            {#each book_store.tag_names as tag (tag)}
                <Chip text={tag} />
            {/each}
        </div>
    </ListSingleItemEntry>

    <ListSingleItemEntry
        key         = "NOTES"
        placeholder = "None set. Tap to add your thoughts on this book."
        href        = { `${current_path}/notes` }
        hasContent  = { book_store.has_notes }
    >
        {@html marked(book_store.notes ?? "")}
    </ListSingleItemEntry>

    <ListSingleItemEntry
        key         = "BLURB"
        placeholder = "None set. Tap to add a blurb (description) to this book."
        href        = { `${current_path}/blurb` }
        hasContent  = { book_store.has_blurb }
    >
        {@html marked(book_store.blurb ?? "")}
    </ListSingleItemEntry>

    <ListSingleItemEntry 
        key         = "VERSIONS"
        placeholder = "None added. Tap to modify."
        href        = { `${current_path}/editions` }
        hasContent  = { book_store.has_editions }
    >
        <div class="versions-wrapper">
            <div class="chips">
                {#each book_store.edition_types as format (format)}
                    <ChipFormat format={format} />
                {/each}
            </div>

            <span>{ book_store.edition_message }</span>
        </div>
    </ListSingleItemEntry>

    {#if book_store.is_new_book === false}
        <Button 
            text = "Delete book"
            type = "secondary"
            onClick = { deleteBook }
        />
    {/if}
</div>



<style>
    .body {
        padding: 16px;
        gap: 32px;
        display: flex;
        flex-direction: column;
    }

    .heading {
        padding: 0 8px;
        font-weight: 700;
        color: var(--text-secondary);
        letter-spacing: 0.05em;
    }

    .list {
        display: flex;
        flex-direction: column;
    }

    .tags {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 4px;
    }

    .cover-wrapper {
        display: flex;
        flex-direction: row;
        gap: 16px;
        align-items: center;

        & .info {
            display: flex;
            flex-direction: column;
            gap: 12px;

            & .title {
                font-weight: 500;
                font-size: large;
            }

            & .unset {
                font-style: italic;
                font-weight: 400;
            }


            & .message {
                color: var(--text-secondary);
            }
        }
    }

    .versions-wrapper {
        display: flex;
        flex-direction: column;
        gap: 12px;

        & span {
            padding: 0 8px;
            font-weight: 500;
        }

        & .chips {
            display: flex;
            flex-direction: row;
            flex-wrap: wrap;
            gap: 4px;
        }
    }

    .banner {
        text-align: center;
        color: var(--red-primary);
    }
</style>