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
    width={width}
    height={height} 
    viewBox="0 0 24 24" 
    fill="none" 
    stroke="currentColor" 
    stroke-width="2" 
    stroke-linecap="round" 
    stroke-linejoin="round"
>
    <path d="M12.4 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7.4"/>
    <circle cx="7.5" cy="7.5" r="1.5"/>
    <path d="m3 18 3.086-3.086a2 2 0 0 1 2.828 0L15 21"/>
    <path d="M21.378 5.626a1 1 0 1 0-3.004-3.004l-5.01 5.012a2 2 0 0 0-.506.854l-.837 2.87a.5.5 0 0 0 .62.62l2.87-.837a2 2 0 0 0 .854-.506z"/>
</svg>