<script lang="ts">
    import RadioSelector from "./radio-selector.svelte";


    
    let {
        label,
        name,
        value,
        checked,
        onchange
    } : {
        label: string
        name: string
        value: string
        checked: boolean
        onchange?: (value: string) => void
    } = $props()
</script>



<label class="entry">
    <input 
        type = "radio"
        name = {name}
        value = {value}
        checked = {checked}
        onchange = { () => onchange?.(value) }
    >
    <span>{label}</span>
    <RadioSelector selected = {checked} />
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