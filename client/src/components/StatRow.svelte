<script lang="ts">
    let {
        stats,
        centered = true
    } : {
        stats: { label: string, value: string }[]
        centered?: boolean
    } = $props()
</script>



<div class = "stat-row">
    {#each stats as stat (stat.label)}
        <div 
            class = "stat"
            class:centered = { centered }
        >
            <span class = "label">{ stat.label.toUpperCase() }</span>
            <span class = "value">{ stat.value }</span>
        </div>
    {/each}
</div>



<style>
    .stat-row
    {
        display: flex;
        flex-direction: row;
        padding: 1rem;
        background-color: var(--raised);
        border: 1px solid var(--raised-border);
        border-radius: 8px;
        box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.25);
    }

    .stat
    {
        display: flex;
        flex-direction: column;
        flex: 1;
        padding: 4px 0;
        gap: 6px;
        font-weight: 500;
        font-size: 1rem;

        &:last-child
        {
            text-align: right;
        }

        .label
        {
            color: var(--text-secondary);
        }

        &.centered
        {
            text-align: center;
        }
    }
</style>