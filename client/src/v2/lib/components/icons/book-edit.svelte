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
    <path d="M13.4 2H6.5A2.5 2.5 0 0 0 4 4.5v15"/>
    <path d="M20 12.6V21a1 1 0 0 1-1 1H6.5a1 1 0 0 1 0-5H20"/>
    <path d="M21.378 5.626a1 1 0 1 0-3.004-3.004l-5.01 5.012a2 2 0 0 0-.506.854l-.837 2.87a.5.5 0 0 0 .62.62l2.87-.837a2 2 0 0 0 .854-.506z"/>
</svg>