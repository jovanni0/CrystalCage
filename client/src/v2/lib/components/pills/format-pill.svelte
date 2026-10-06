<script lang="ts">
    let {
        label,
        hue,
        chroma,
        is_disabled = false,
    }: {
        label:        string,
        hue:          number
        chroma?:      number
        is_disabled?: boolean
    } = $props()
</script>



<format-pill
    class:disabled      = { is_disabled }
    style:--pill-hue    = { hue }
    style:--pill-chroma = { chroma }
>
    { label }
</format-pill>



<style>
    format-pill
    {
        --pill-base: oklch(0.47 var(--pill-chroma, 0.09) var(--pill-hue));

        padding: 4px 8px;
        border-radius: 999px;

        background-color: var(--pill-base);
        color: var(--text-inverted);
        
        font-size: var(--text-caption);

        &.disabled
        {
            background-color: color-mix(in oklch, var(--pill-base) 65%, var(--bg));
        }
    }
</style>