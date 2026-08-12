<script lang="ts">
    import Checkbox from "./checkbox.svelte";

    let {
        label,
        value,
        checked,
        onchange
    } : {
        label: string
        value: string
        checked: boolean
        onchange?: (value: string, checked: boolean) => void
    } = $props()
</script>



<label class="entry">
    <input 
        type = "checkbox"
        value = { value }
        checked = { checked }
        onchange = { () => onchange?.(value, !checked) }
    >
    <span>{label}</span>
    <Checkbox selected = { checked } />
</label>



<style>
    input {
        display: none;
    }

    .entry {
        display: flex;
        flex-direction: row;
        align-items: center;
        padding: 12px;
        border-bottom: 1px solid var(--dividers);
    }

    span {
        flex: 1;
    }
</style>