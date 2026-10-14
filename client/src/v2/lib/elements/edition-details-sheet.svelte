<script lang="ts">
    import BottomSheet from "../components/_other/bottom-sheet.svelte";
    import Button from "../components/buttons/button.svelte";
    import IconButton from "../components/buttons/icon-button.svelte";
    import Trash from "../components/icons/trash.svelte";
    import KeyValueInsertListItem from "../components/list-items/key-value-insert-list-item.svelte";
    import FormatPill from "../components/pills/format-pill.svelte";
    import type { EditionFileFormatDefinition } from "../utils/format-color-registry.svelte";



    let {
        is_open = $bindable(),
        label,
        format,
        release_date,
        read_status,
        file_size,
        added_at,
        is_downloadable,
        onTrash,
        onDownload,
    }: {
        is_open:         boolean
        label:           string
        format:          EditionFileFormatDefinition
        release_date:    string
        read_status:     string
        file_size:       string
        added_at:        string
        is_downloadable: boolean
        onTrash?:        () => void
        onDownload?:     () => void
    } = $props()
</script>



<BottomSheet
    bind:is_open = { is_open }
>
    {#snippet header()}
        <p class = "edition-label">{ label }</p>
    {/snippet}

    <details-list>
        <KeyValueInsertListItem key = "Format">
            <FormatPill
                label  = { format.label }
                hue    = { format.hue }
                chroma = { format.chroma}
            />
        </KeyValueInsertListItem>

        <KeyValueInsertListItem key = "Release date">
            <p>{ release_date }</p>
        </KeyValueInsertListItem>

        <KeyValueInsertListItem key = "Status">
            <p>{ read_status }</p>
        </KeyValueInsertListItem>

        <KeyValueInsertListItem key = "File size">
            <p>{ file_size }</p>
        </KeyValueInsertListItem>

        <KeyValueInsertListItem key = "Added at">
            <p>{ added_at }</p>
        </KeyValueInsertListItem>
    </details-list>

    {#snippet footer()}
        <buttons-wrapper>
            <IconButton
                label   = "delete edition"
                shape   = "round"
                shade   = "danger"
                onclick = { onTrash }
            >
                <Trash />
            </IconButton>

            <Button
                class   = "primary-action"
                state   = { is_downloadable ? "default" : "disabled" }
                onclick = { onDownload }
            >
                Download file
            </Button>
        </buttons-wrapper>
    {/snippet}
</BottomSheet>



<style>
    p.edition-label
    {
        font-weight: 600;
        font-size: var(--text-body-lg);
        text-align: center;
        line-clamp: 3;
    }

    buttons-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 4px;
        margin-top: 16px;

        & > :global(button.primary-action)
        {
            flex: 1;
        }
    }
</style>