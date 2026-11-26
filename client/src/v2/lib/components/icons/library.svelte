<script lang="ts">
    let {
        width = 24,
        height = 24
    } : {
        width?: number
        height?: number
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
    class="lucide lucide-library preview-icon"
>
    <path d="m16 6 4 14"/>
    <path d="M12 6v14"/>
    <path d="M8 8v12"/>
    <path d="M4 4v16"/>
</svg>