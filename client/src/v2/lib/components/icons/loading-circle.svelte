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
    width = { width }
    height = { height }
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    stroke-width="2" 
    stroke-linecap="round" 
    stroke-linejoin="round" 
    class={["lucide lucide-loader-circle preview-icon", className ]}
>
    <path d="M21 12a9 9 0 1 1-6.219-8.56"/>
</svg>