<script lang="ts">
    let {
        label,
        color,
    }: {
        label: string
        color?: string
    } = $props()
</script>



<generic-pill
    style:--pill-bg-color = { color }
>
    { label }
</generic-pill>



<style>
    generic-pill
    {
        padding: 6px 12px;
        border-radius: 999px;
        border: 1px solid var(--text-muted);
        background-color: var(--pill-bg-color, transparent);
    }
</style>