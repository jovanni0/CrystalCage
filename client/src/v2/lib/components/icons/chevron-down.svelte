<script lang="ts">
    let {
        width = 24,
        height = 24,
        class: className,
    } : {
        width?: number
        height?: number
        class?: string
    } = $props()
</script>



<svg 
    xmlns="http://www.w3.org/2000/svg" 
    width={width} 
    height={height} 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    stroke-width="2" 
    stroke-linecap="round" 
    stroke-linejoin="round" 
    class={[ "lucide lucide-chevron-down preview-icon", className ]}
>
    <path d="m6 9 6 6 6-6"/>
</svg>