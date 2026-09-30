<script lang="ts">
    let {
        format
    } : {
        format: string
    } = $props()
</script>



<span 
    class:epub={format.toLowerCase() === "epub"}
    class:mobi={format.toLowerCase() === "mobi"}
    class:fvb={format.toLowerCase() === "fvb"}
>
    {format.toUpperCase()}
</span>



<style>
    span {
        padding: 4px 8px;
        border-radius: 100px;
        color: var(--text-secondary);
        background-color: var(--dividers);
        font-size: small;
        font-weight: 500;
    }

    .epub {
        color: var(--peach-text);
        background-color: var(--peach-secondary);
    }

    .mobi {
        color: var(--mauve-text);
        background-color: var(--mauve-secondary);
    }

    .fvb {
        color: var(--teal-text);
        background-color: var(--teal-secondary);
    }
</style>