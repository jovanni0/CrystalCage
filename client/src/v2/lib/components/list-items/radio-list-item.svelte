<script lang="ts">
    import { generateEphemeralId } from "@root/utils/IdGenerator";
    import Radiobox from "../boxes/radiobox.svelte";



    let {
        label,
        is_selected = false,
        onSelect,
    }: {
        label: string
        is_selected?: boolean
        onSelect?: () => void
    } = $props()


    let radio_id = generateEphemeralId()
</script>



<label for = { radio_id }>
    <p>{ label }</p>
    <Radiobox is_selected = { is_selected } />
    <input 
        type = "radio"
        id = { radio_id }
        checked = { is_selected }
        onchange = { onSelect }
    >
</label>



<style>
    input
    {
        display: none;
    }

    label
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        padding: 16px 8px;
        border-bottom: 1px solid var(--text-placeholder);
        cursor: pointer;

        & > p
        {
            flex: 1;
        }
    }
</style>