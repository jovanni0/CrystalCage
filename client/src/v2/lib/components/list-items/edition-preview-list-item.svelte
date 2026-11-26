<script lang="ts">
    import type { EditionFileFormatDefinition } from "@v2/lib/utils/format-color-registry.svelte";
    import FormatPill from "../pills/format-pill.svelte";
    import ChevronRight from "../icons/chevron-right.svelte";
    import { ripple_effect } from "../buttons/ripple-effect";
    import "../buttons/ripple-effect.css"

    let {
        id,
        format,
        label,
        read_status,
        release_date,
        onClick,
    }: {
        id:           string
        format:       EditionFileFormatDefinition
        label:        string
        read_status:  string
        release_date: string | null
        onClick?:     () => void
    } = $props()
</script>



<button
    class = "ripple-container"
    onclick = { onClick }
    {@attach ripple_effect}
>
    <left-wrapper>
        <top-wrapper>
            <FormatPill 
                label  = { format.label }
                hue    = { format.hue }
                chroma = { format.chroma }
            />
        
            <p class = "edition-label">{ label }</p>
        </top-wrapper>

        {#if release_date}
            <p class = "release-date">{ `Released ${release_date}` }</p>
        {/if}
    </left-wrapper>

    <right-wrapper>
        <p class = "read-status">{ read_status }</p>

        <ChevronRight />
    </right-wrapper>
</button>



<style>
    button
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        min-height: 99px;

        border-bottom: 1px solid var(--text-placeholder);

        padding: 24px 0 24px 8px;
        gap: 8px;

        left-wrapper
        {
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex: 1;

            top-wrapper
            {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 8px;

                p.edition-label
                {
                    flex: 1;
                    line-clamp: 1;
                    font-weight: 600;
                }
            }

            p.release-date
            {
                font-size: var(--text-caption);
                color: var(--text-muted);
                min-height: var(--text-caption);
            }
        }

        right-wrapper
        {
            display: flex;
            flex-direction: row;
        }
    }

</style>