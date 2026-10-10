<script lang="ts">
    import type { SVGAttributes } from "svelte/elements";



    let {
        size = 16,
        class: klass = "",
        ...rest
    }: SVGAttributes<SVGSVGElement> & {
        size?: number
    } = $props()
</script>



<svg
    width   = { size }
    height  = { size }
    viewBox = "0 0 24 24"
    fill    = "none"
    class   = "spinner { klass }"
    { ...rest }
>
    <circle 
        cx = "12"
        cy = "12"
        r  = "10"
        stroke            = "currentColor"
        stroke-width      = "inherit"
        stroke-linecap    = "round"
        stroke-dasharray  = "90"
        stroke-dashoffset = "45"
    />
</svg>



<style>
    .spinner
    {
        animation: spin 1.2s linear infinite;
    }

    @keyframes spin
    {
        to 
        { 
            transform: rotate(360deg); 
        }
    }
</style>