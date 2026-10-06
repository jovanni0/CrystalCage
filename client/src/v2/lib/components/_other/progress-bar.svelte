<script lang="ts">
    let {
        percentage = 0
    }: {
        percentage?: number
    } = $props()

    const value: number = $derived(Math.min(Math.max(Math.round(percentage * 100), 0), 100))
</script>



<progress-bar>
    <progress-track
        style:--progress = "{ value }%"
    ></progress-track>

    <p class = progress-counter>{ value }%</p>
</progress-bar>



<style>
    progress-bar
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;
    }

    progress-track
    {
        position: relative;
        background-color: var(--bg-inverted-muted);
        border-radius: 999px;
        overflow: hidden;
        flex: 1;
        height: 8px;

        &::before
        {
            content: "";
            position: absolute;
            inset: 0 auto 0 0;
            width: clamp(4px, var(--progress), 100%);
            background-color: var(--text-muted);
        }
    }
</style>