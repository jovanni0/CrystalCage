<script lang="ts">
    import Check from "../icons/check.svelte";



    let { 
        is_checked = false 
    } : { 
        is_checked: boolean 
    } = $props()
</script>



<checkbox
    class:checked = { is_checked }
>
    {#if is_checked }
        <Check 
            width = { 16 }
            height = { 16 }
        />
    {/if}
</checkbox>



<style>
    checkbox
    {
        display: flex;
        align-items: center;
        justify-content: center;

        width: 20px;
        height: 20px;

        border-radius: 4px;
        border: 1px solid var(--text);
        background-color: transparent;
    }

    checkbox.checked
    {
        background-color: var(--bg-brand);
        border: 1px solid var(--bg-brand);
        color: var(--text-inverted);
    }
</style>