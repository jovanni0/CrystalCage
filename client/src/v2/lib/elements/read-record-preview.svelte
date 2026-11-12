<script lang="ts">
    import type { EditionFileFormatDefinition } from "../utils/format-color-registry.svelte";
    import FormatPill from "../components/pills/format-pill.svelte";
    import ChevronRight from "@root/components/icons/chevron-right.svelte";
    import Button from "../components/buttons/button.svelte";
    import { stopPropagation } from "svelte/legacy";


    let {
        id,
        index,
        format,
        rendered_note,
        start_date,
        end_date,
        is_in_progress,
        is_historical,
        onOpenDetails,
        onFinishRead,
    }: {
        id: string
        index: number
        format: EditionFileFormatDefinition
        rendered_note: string | null
        start_date: string | null
        end_date: string | null
        is_in_progress: boolean
        is_historical: boolean
        onOpenDetails: () => void
        onFinishRead?: () => void
    } = $props()
</script>


<read-record-preview 
    class:in-progress = { is_in_progress }
>
    <button 
        class = "header-wrapper"
        onclick = { onOpenDetails }
    >
        <FormatPill 
            label       = { format.label }
            hue         = { format.hue }
            chroma      = { format.chroma }
            is_disabled = { false }
        />

        {#if is_in_progress}
            <p class = "currently-reading">Currently reading</p>
        {:else}
            <p class = "read-count">Read #{ index + 1 }</p>
        {/if}

        <ChevronRight />
    </button>

    {#if rendered_note}
        <note-wrapper>
            {@html rendered_note}
        </note-wrapper>
    {/if}

    <dates-wrapper>
        <p class = "start-date">{ start_date }</p>

        {#if is_historical}
            <p class = "end-date">Historical</p>
        {:else if is_in_progress}
            <Button 
                size    = "slim"
                onclick = { (event) => { event.stopPropagation(); onFinishRead?.() } }
            >
                Finish
            </Button>
        {:else}
            <p class = "end-date">{ end_date }</p>
        {/if}
    </dates-wrapper>
</read-record-preview>



<style>
    read-record-preview
    {
        position: relative;
        display: flex;
        flex-direction: column;
        gap: 16px;
        border-bottom: 1px solid var(--text-placeholder);
        padding: 12px 0 24px 8px;

        &.in-progress
        {
            padding-left: 16px;
        }

        &.in-progress::before
        {
            content: "";

            position: absolute;
            width: 4px;
            inset: 12px auto 24px 0;
            border-radius: 999px;

            background-color: var(--bg-brand-secondary);
        }
    }


    button.header-wrapper
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;

        p.read-count,
        p.currently-reading
        {
            flex: 1;
            font-size: var(--text-body);
            font-weight: 600;
        }

        p.read-count
        {
            color: var(--text-muted);
        }

        p.currently-reading
        {
            color: var(--text-brand-secondary);
        }
    }

    note-wrapper
    {
        padding-right: 8px;
        text-align: justify;
    }


    dates-wrapper
    {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        padding-right: 8px;
        
        p.start-date,
        p.end-date
        {
            font-weight: 600;
        }
    }
</style>