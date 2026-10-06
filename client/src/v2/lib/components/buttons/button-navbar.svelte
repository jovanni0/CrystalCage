<script lang="ts">
    import      { ripple_effect } from "./ripple-effect";
    import      "./ripple-effect.css"
    import type { Snippet } from "svelte";



    let {
        label,
        children,
        onClick,
    }: {
        label:    string
        children: Snippet
        onClick?: () => void
    } = $props()
</script>



<button
    class   = "ripple-container"
    onclick = { onClick }
    {@attach ripple_effect}
>
    {@render children?.()}
    <p class = "button-label">{ label }</p>
</button>



<style>
    button
    {
        height: 48px;
        padding: 0 16px;

        border-radius: 8px;

        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        user-select: none;
        cursor: pointer;
        overflow: hidden;
    }

    p
    {
        font-size: var(--text-caption);
        text-align: center;
    }
</style>