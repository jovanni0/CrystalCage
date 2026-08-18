<script lang="ts">
    import Button from "@root/components/button.svelte";
    import CategorySeparator from "@root/components/category-separator.svelte";
    import FormatChip        from "@root/components/FormatChip.svelte";
    import StatRow           from "@root/components/StatRow.svelte";
    import { formatColor }   from "@root/utils/formatColor";


    let {
        record_id,
        read_number,
        start_date,
        end_date,
        notes,
        edition_format,
        edition_label,
        edition_added_at,
        in_progress,
        onFinish,
    } : {
        record_id:        string
        read_number:      number
        start_date:       string
        end_date:         string
        notes:            string | null
        edition_format:   string
        edition_label:    string
        edition_added_at: string
        in_progress:      boolean
        onFinish?:        (() => void | Promise<void>) | undefined
    } = $props()
</script>


<li 
    class = "read-list-item"
    class:in-progress = { in_progress }
>
    {#if in_progress}
        <span class = "currently-reading">Currently reading</span>
    {:else}
        <CategorySeparator 
            title = { `Read #${read_number}` }
            href  = { `/read-info/${record_id}` }
        />
    {/if}

    <StatRow 
        stats = {[
            { label: "started", value: start_date },
            { label: "finished", value: end_date },
        ]}
        centered = { false }
    />

    <div class = "edition-info">
        <FormatChip
            format = { edition_format }
            color  = { formatColor(edition_format) }
        />

        <span>{ edition_label } · { edition_added_at }</span>

        {#if in_progress}
            <Button 
                text = "Finish"
                onClick = { onFinish }
            />
        {/if}
    </div>

    {#if notes}
        <span class = "notes">{ notes }</span>
    {/if}
</li>


<style>
    .read-list-item
    {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        border-bottom: 1px solid var(--dividers);
        padding: 0.5rem 0.75rem 1.5rem 0.75rem;

        &.in-progress
        {
            border-left: 6px solid var(--amber-background);
        }
    }

    .edition-info
    {
        display: flex;
        flex-direction: row;
        gap: 0.5rem;

        span
        {
            display: flex;
            align-items: center;
            font-size: 0.8rem;
            color: var(--text-secondary);
            font-weight: 500;
            flex: 1;
        }
    }

    .currently-reading
    {
        font-weight: 500;
        font-size: large;
        color: var(--amber-primary);
    }
</style>