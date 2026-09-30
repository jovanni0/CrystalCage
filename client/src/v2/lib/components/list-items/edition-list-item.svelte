<script lang="ts">
    import FormatPill from "../pills/format-pill.svelte";
    import Radiobox from "../boxes/radiobox.svelte";
    import type { FormatColorVariant } from "@v2/lib/utils/format-color-registry";


    
    let {
        variant,
        is_checked  = false,
        is_disabled = false,
        onChange,
    }: {
        variant:     FormatColorVariant
        is_checked:  boolean
        is_disabled: boolean
        onChange?:   () => void
    } = $props()
</script>



<label
    class          = "edition-list-item"
    class:disabled = { is_disabled }
>
    <FormatPill 
        label       = { variant.label }
        color       = { variant.color }
        is_disabled = { is_disabled }
    />

    <wrapper>
        <p>Nemira</p>
        <subtitle>Added 07-07-2021</subtitle>
    </wrapper>

    <Radiobox 
        is_selected = { is_checked } 
        is_disabled = { is_disabled }
    />
    <input 
        class    = "hidden"
        type     = "radio"
        checked  = { is_checked }
        onchange = { onChange }
    />
</label>



<style>
    .edition-list-item
    {
        display: flex;
        flex-direction: row;
        gap: 8px;
        padding: 16px 8px;
        align-items: center;

        border-bottom: 1px solid var(--text-placeholder);

        wrapper
        {
            display: flex;
            flex-direction: column;
            gap: 4px;
            flex: 1;
        }
    
        subtitle
        {
            font-size: var(--text-caption);
            color: var(--text-muted);
        }
    }

    .edition-list-item.disabled
    {
        color: var(--text-placeholder);

        subtitle
        {
            color: var(--text-placeholder);
        }
    }
</style>