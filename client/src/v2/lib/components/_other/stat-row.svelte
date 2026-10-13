<script lang="ts">
    let {
        stats,
    } : {
        stats: { label: string, value: string }[]
    } = $props()
</script>



<stat-row>
    {#each stats as stat (stat.label)}
        <stat-wrapper>
            <p class = "label">{ stat.label.toUpperCase() }</p>
            <p class = "value">{ stat.value }</p>
        </stat-wrapper>
    {/each}
</stat-row>



<style>
    stat-row
    {
        display: flex;
        flex-direction: row;
        padding: 12px 0;
        background-color: var(--bg-overlay);
        border: 1px solid var(--text-inverted-muted);
        border-radius: 8px;
        box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.25);
    }

    stat-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        flex: 1;
        gap: 4px;
    }

    p.label
    {
        color: var(--text-muted);
        font-weight: 600;
        font-size: var(--text-body-sm);
        letter-spacing: 10%;
    }
</style>