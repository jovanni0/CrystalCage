<script lang="ts">
    let {
        label,
        color,
        is_disabled = false,
    }: {
        label:        string,
        color:        string
        is_disabled?: boolean
    } = $props()
</script>



<format-pill
    class:disabled = { is_disabled }
    style:--pill-color = { color }
>
    { label }
</format-pill>



<style>
    format-pill
    {
        padding: 4px 8px;
        border-radius: 999px;
        background-color: var(--pill-color);
        color: var(--text-inverted);
        font-size: var(--text-caption);

        &.disabled
        {
            background-color: color-mix(in oklch, var(--pill-color) 70%, white 30%);
        }
    }
</style>