<script lang="ts">
    let {
        format,
        color
    } : {
        format: string
        color:  string
    } = $props()
</script>



<div 
    class = "chip"
    style:--chip-color = { color }
>
    <span>{ format }</span>
</div>



<style>
    .chip {
        padding: 0.25rem 0.5rem;
        border-radius: 100px;
        flex: 0 0 auto;
        background-color: color-mix(in srgb, var(--chip-color) 15%, transparent);
        color: var(--chip-color);
        font-size: 0.8rem;
        font-weight: 500;
    }
</style>