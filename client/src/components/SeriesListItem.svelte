<script lang="ts">
    import type { ReadStatus } from "@root/types/ReadStatus";
    import CoverPlaceholder from "./cover-placeholder.svelte";
    import CoverImage from "./CoverImage.svelte";
    import ReadStatusIndicator from "./ReadStatusIndicator.svelte";


    let {
        book_id,
        cover_id,
        title,
        date,
        words,
        status,
        read_count
    } : {
        book_id:    string
        cover_id:   string | null
        title:      string
        date:       string | null
        words:      number | null
        status:     ReadStatus
        read_count: number
    } = $props()
</script>



<li>
    <a 
        class = "series-item"
        href  = { `/book/${book_id}` }
    >
        <div class = "image">
            {#if cover_id}
                <CoverImage 
                    cover_id = { cover_id }
                    alt      = { title }
                />
            {:else}
                <CoverPlaceholder />
            {/if}
        </div>

        <div class="info">
            <span class = "title">{ title }</span>
            <dl class = "metadata">
                <dt>Release:</dt>
                <dd>{ date ? new Date(date).getFullYear() : "(Unset)" }</dd>

                <dt>Words</dt>
                <dd>{ words !== null ? words : "(Unset)" }</dd>
            </dl>

            <ReadStatusIndicator 
                status     = { status }
                read_count = { read_count }
            />
        </div>
    </a>
</li>



<style>
    li
    {
        list-style: none;
    }

    .series-item
    {
        display: flex;
        flex-direction: row;
        padding: 16px;
        gap: 32px;
        border-bottom: 1px solid var(--dividers);
        color: inherit;
        text-decoration: none;
    }

    .image
    {
        width: 130px;
        aspect-ratio: 2/3;
        border-radius: 8px;
        overflow: hidden;
    }

    .info
    {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        padding: 8px 0;

        .title
        {
            font-size: large;
            font-weight: 500;
        }
    }

    .metadata
    {
        display: grid;
        grid-template-columns: auto 1fr;
        column-gap: 8px;
        row-gap: 8px;
        margin: 0;
        color: var(--text-secondary);
    }
</style>