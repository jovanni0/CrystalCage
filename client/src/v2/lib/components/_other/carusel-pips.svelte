<script lang="ts">
    let {
        count,
        current
    }: {
        count:   number
        current: number
    } = $props()
</script>



<carusel-pips>
    {#each Array(count) as _, index}
        <span class:active = { index === current}></span>
    {/each}
</carusel-pips>



<style>
    carusel-pips
    {
        display: flex;
        flex-direction: row;
        gap: 4px;
    }

    span
    {
        width: 6px;
        aspect-ratio: 1;
        border-radius: 100%;
        background-color: var(--bg-inverted-muted);

        &.active
        {
            background-color: var(--text-muted);
        }
    }
</style>