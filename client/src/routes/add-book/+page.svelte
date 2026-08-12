<script lang="ts">
    import Chip                  from "@root/components/chip.svelte";
    import ChipFormat            from "@root/components/chip-format.svelte";
    import ListEntry             from "@root/components/list-entry.svelte";
    import ListEntryDate         from "@root/components/list-entry-date.svelte";
    import ListEntryNumber       from "@root/components/list-entry-number.svelte";
    import ListEntryText         from "@root/components/list-entry-text.svelte";
    import ListSingleItemEntry   from "@root/components/list-single-item-entry.svelte";
    import { marked }            from "marked";
    import { manage_book_store } from "@root/CompositionRoot";
    import TopbarConfirmCancel   from "@root/components/topbar-confirm-cancel.svelte";


    let error_message = $state<string|null>(null)

    async function save()
    {
        const result = await manage_book_store.save()

        if (result)
        {
            cancel()
        }

        error_message = "An error occured while tring to save the book"
    }


    async function cancel()
    {
        manage_book_store.reset()
        error_message = null
    }
</script>



<TopbarConfirmCancel
    title     = "Add Book"
    onCancel  = { cancel }
    onConfirm = { save }
/>

<div class="body">
    {#if error_message !== null}
        <span class="banner">{ error_message }</span>
    {/if}

    <ListSingleItemEntry 
        key         = "COVERS"
        placeholder = "None added. Tap to modify."
        href        = "/add-book/covers"
        hasContent  = { manage_book_store.has_covers }
    >
        <div class="cover-wrapper">
            <img 
                src   = { manage_book_store.default_cover?.preview_url }
                alt   = "default cover"
                class = "cover-image"
            />

            <div class="info">
                <span 
                    class       = "title"
                    class:unset = { manage_book_store.default_cover === null }
                >
                    { manage_book_store.default_cover?.title ?? "(No title set)" }
                </span>
                <span class="message">{ manage_book_store.cover_message }</span>
            </div>
        </div>
    </ListSingleItemEntry>

    <div class="list">
        <span class="heading">IDENTITY</span>

        <ListEntry
            key   = "Author"
            value = { manage_book_store.author_name ?? undefined }
            href  = "/add-book/author"
        />
        <ListEntry
            key   = "Universe"
            value = { manage_book_store.universe_name ?? undefined }
            href  = "/add-book/universe"
        />
        <ListEntry
            key   = "Series"
            value = { manage_book_store.series_name ?? undefined }
            href  = "/add-book/series"
        />
        <ListEntryNumber
            key        = "Volume"
            bind:value = {
                () => manage_book_store.volume_number ?? undefined,
                (v) => manage_book_store.volume_number = v ?? null
            }
        />
        <ListEntryText
            key        = "Title"
            bind:value = {
                () => manage_book_store.title ?? undefined,
                (v) => manage_book_store.title = v ?? null
            }
        />
    </div>

    <div class="list">
        <span class="heading">DETAILS</span>

        <ListEntryDate
            key        = "Release date"
            bind:value = {
                () => manage_book_store.launch_date?.toISOString().slice(0, 10) ?? "",
                (v) => manage_book_store.launch_date = v ? new Date(v) : null
            }
        />

        <ListEntryNumber
            key        = "Word Count"
            bind:value = {
                () => manage_book_store.word_count ?? undefined,
                (v) => manage_book_store.word_count = v ?? null
            }
        />
    </div>

    <ListSingleItemEntry
        key         = "TAGS"
        placeholder = "None set. Tap to select from a preset list or customize from scratch."
        href        = "/add-book/tags"
        hasContent  = { manage_book_store.cover_count > 0 }
    >
        <div class="tags">
            {#each manage_book_store.tag_names as tag (tag)}
                <Chip text={tag} />
            {/each}
        </div>
    </ListSingleItemEntry>

    <ListSingleItemEntry
        key         = "NOTES"
        placeholder = "None set. Tap to add your thoughts on this book."
        href        = "/add-book/notes"
        hasContent  = { manage_book_store.has_notes }
    >
        {@html marked(manage_book_store.notes ?? "")}
    </ListSingleItemEntry>

    <ListSingleItemEntry
        key         = "BLURB"
        placeholder = "None set. Tap to add a blurb (description) to this book."
        href        = "/add-book/blurb"
        hasContent  = { manage_book_store.has_blurb }
    >
        {@html marked(manage_book_store.blurb ?? "")}
    </ListSingleItemEntry>

    <ListSingleItemEntry 
        key         = "VERSIONS"
        placeholder = "None added. Tap to modify."
        href        = "/add-book/editions"
        hasContent  = { manage_book_store.has_editions }
    >
        <div class="versions-wrapper">
            <div class="chips">
                {#each manage_book_store.edition_types as format (format)}
                    <ChipFormat format={format} />
                {/each}
            </div>

            <span>{ manage_book_store.edition_message }</span>
        </div>
    </ListSingleItemEntry>
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

        & .cover-image {
            width: 75px;
            height: 112.5px;
            border-radius: 8px;
            object-fit: cover;
        }

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