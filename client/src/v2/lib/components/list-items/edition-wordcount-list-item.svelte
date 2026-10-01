<script lang="ts">
    import type { EditionFileFormatDefinition } from "@v2/lib/utils/format-color-registry.svelte";
    import FormatPill from "../pills/format-pill.svelte";


    let {
        label,
        added_at,
        word_count,
        format_color_definition,
    }: {
        label:                   string
        added_at:                string
        word_count?:             number
        format_color_definition: EditionFileFormatDefinition
    } = $props()
</script>



<edition-wordcount-list-item
    class:disabled = { !word_count }
>
    <left-wrapper>
        <p class = "edition-label">{ label }</p>

        <info-wrapper>
            <FormatPill 
                label       = { format_color_definition.label }
                hue         = { format_color_definition.hue }
                chroma      = { format_color_definition.chroma }
                is_disabled = { !word_count }
            />

            <p class = "added-at-label">{ added_at }</p>
        </info-wrapper>
    </left-wrapper>

    <p class = "word-count-label">{ word_count ? `~ ${word_count} K` : "Unknown" }</p>
</edition-wordcount-list-item>



<style>
    edition-wordcount-list-item
    {
        display: flex;
        flex-direction: row;
        gap: 16px;
        align-items: center;
        padding: 16px 8px;
        border-bottom: 1px solid var(--text-placeholder);

        &.disabled
        {
            color: var(--text-placeholder);
        }
    }

    left-wrapper
    {
        display: flex;
        flex-direction: column;
        flex: 1;
        gap: 8px;
    }

    info-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 4px;
        align-items: center;
    }

    .edition-label,
    .word-count-label
    {
        font-weight: 500;
    }

    .added-at-label
    {
        color: var(--text-muted);
        font-size: var(--text-caption);
    }
</style>