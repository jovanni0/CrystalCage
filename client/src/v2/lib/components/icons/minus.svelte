<script lang="ts">
    let {
        width = 24,
        height = 24
    } : {
        width?: number
        height?: number
    } = $props()
</script>


<!-- ------------------------------------ -->


<svg 
    xmlns="http://www.w3.org/2000/svg" 
    width={ width } 
    height={ height } 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    stroke-width="2" 
    stroke-linecap="round" 
    stroke-linejoin="round" 
    class="lucide lucide-plus preview-icon"
>
    <path d="M5 12h14"/>
</svg>