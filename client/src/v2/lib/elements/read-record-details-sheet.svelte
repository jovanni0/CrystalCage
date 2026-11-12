<script lang="ts">
    import Pencil from "@root/icons/Pencil.svelte";
    import BottomSheet from "../components/_other/bottom-sheet.svelte";
    import IconButton from "../components/buttons/icon-button.svelte";
    import Trash from "../components/icons/trash.svelte";
    import KeyValueInsertListItem from "../components/list-items/key-value-insert-list-item.svelte";
    import FormatPill from "../components/pills/format-pill.svelte";
    import type { EditionFileFormatDefinition } from "../utils/format-color-registry.svelte";
    import { renderMarkdown } from "../utils/markdown";



    let {
        is_open = $bindable(),
        read_number,
        start_date,
        end_date,
        total_time,
        edition_label,
        edition_format,
        state,
        is_historical,
        notes,
        onEdit,
        onDelete,
    }: {
        is_open:        boolean
        read_number:    number
        start_date:     string | null
        end_date:       string | null
        total_time:     number | null
        edition_label:  string | null
        edition_format: EditionFileFormatDefinition | null
        state:          "In progress" | "DNF" | null
        is_historical:  boolean
        notes:          string | null
        onEdit:         () => void
        onDelete:       () => void
    } = $props()


    let notes_html: string | null = $derived(notes ? renderMarkdown(notes) : null)
    let is_no_info: boolean       = $derived(!start_date && !end_date && !(edition_label && edition_format) && !notes)
</script>



<BottomSheet bind:is_open>
    {#snippet header()}
        <h1 class = "read-number">Read #{read_number}</h1>
        <p class = "subheading">{[state, is_historical && "Historical"].filter(Boolean).join(" · ")}</p>
    {/snippet}


    {#if is_no_info}
        <p class = "no-info">There is no information recorded.</p>
    {/if}

    {#if start_date}
        <KeyValueInsertListItem key = "Start date">
            <p class = "start-date">{ start_date }</p>
        </KeyValueInsertListItem>
    {/if}

    {#if end_date}
        <KeyValueInsertListItem key = "End date">
            <p class = "end-date">{ end_date }</p>
        </KeyValueInsertListItem>
    {/if}

    {#if total_time}
        <KeyValueInsertListItem key = "Total time">
            <p class = "total-time">{ total_time } days</p>
        </KeyValueInsertListItem>
    {/if}

    {#if edition_label && edition_format}
        <KeyValueInsertListItem key = "Edition">
            <edition-wrapper>
                <p class = "edition-label">Hardcover</p>
                <FormatPill 
                    label  = { edition_format.label } 
                    hue    = { edition_format.hue }
                    chroma = { edition_format.chroma }
                />
            </edition-wrapper>
        </KeyValueInsertListItem>
    {/if}

    {#if notes_html}
        <notes-container>{@html notes_html}</notes-container>
    {/if}


    {#snippet footer()}
        <buttons-container>
            <IconButton label = "delete" shade = "danger" onclick = { onDelete }>
                <Trash />
            </IconButton>

            <IconButton label = "edit" onclick = { onEdit }>
                <Pencil />
            </IconButton>
        </buttons-container>
    {/snippet}
</BottomSheet>



<style>
    h1.read-number
    {
        font-size: var(--text-body-lg);
        font-weight: 600;
        text-align: center;
    }


    p.subheading
    {
        font-size: var(--text-body-sm);
        color: var(--text-muted);
        text-align: center;
    }


    p.no-info
    {
        text-align: center;
        color: var(--text-muted);
    }


    edition-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 4px;
        align-items: center;
    }


    notes-container
    {
        text-align: justify;
        padding: 16px 0 16px 0;
    }


    buttons-container
    {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        gap: 4px;
    }
</style>