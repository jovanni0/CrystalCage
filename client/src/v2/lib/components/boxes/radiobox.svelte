<script lang="ts">
    let {
        is_selected = false,
        is_disabled = false,
    }: {
        is_selected?: boolean
        is_disabled?: boolean
    } = $props()
</script>



<radiobox 
    class:selected = { is_selected }
    class:disabled = { is_disabled }
></radiobox>



<style>
    radiobox
    {
        display: inline-block;
        box-sizing: border-box;

        width: 20px;
        height: 20px;

        background-color: transparent;
        border: 1px solid var(--text);
        border-radius: 999px;
    }

    radiobox.selected
    {
        border-color: var(--bg-brand);
        background-color: var(--bg-brand);
        padding: 1px;
        background-clip: content-box;
    }

    radiobox.disabled
    {
        border-color: var(--text-placeholder);
        background-color: transparent;
    }
</style>