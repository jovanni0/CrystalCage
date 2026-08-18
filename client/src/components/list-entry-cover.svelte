<script lang="ts">
    import type { CoverSource } from "@root/types/CoverSource";
    import CoverImage from "./CoverImage.svelte";
    import RadioSelector from "./radio-selector.svelte";

    let {
        name,
        radio_value,
        input_value,
        checked,
        image_source,
        onChange,
        onDelete,
        onSelected
    } : {
        name?: string
        radio_value?: string
        input_value?: string
        checked: boolean
        image_source: CoverSource
        onChange?: (value: string) => void
        onDelete?: () => void
        onSelected?: () => void
    } = $props()
</script>

<label class="cover-selector">
    <input 
        type="radio"
        name={name}
        value={radio_value}
        checked={checked}
        onchange={ onSelected }
    >

    <CoverImage 
        source = { image_source } 
        alt    = "cover" 
        width  = "100px"
    />

    <div class="info-body">
        <div class="info">
            <input 
                class="value"
                class:none-set={input_value === undefined || input_value === ""}
                placeholder="(No title set)"
                onchange={(e) => onChange?.(e.currentTarget.value)}
                value={input_value}
                required
            >
            <span class="faded">Added Jan 2019</span>
        </div>

        <div class="info">
            <span class="faded">This is the default cover</span>
            <button class="delete" onclick={(e) => { e.preventDefault(); onDelete?.() }}>
                Delete cover
            </button>
        </div>
    </div>

    <RadioSelector selected={checked} />
</label>



<style>
    .cover-selector {
        display: flex;
        flex-direction: row;
        gap: 24px;
        border-bottom: 1px solid var(--dividers);
        padding: 16px 0;
        align-items: center;
        align-self: stretch;
        cursor: pointer;
    }

    .info-body {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        align-self: stretch;
        padding: 4px 0;
        flex: 1;
    }

    .info {
        display: flex;
        flex-direction: column;
        gap: 4px;
        
        & > * {
            padding: 0 8px;
        }
    }

    .faded {
        color: var(--text-secondary);
    }

    .value {
        all: unset;
        flex: 1;
        width: 100%;
        box-sizing: border-box;
        background-color: var(--input-fill);
        padding: 4px 8px;
        border-radius: 4px;
        color: var(--text);

        &.none-set {
            font-style: italic;
            color: var(--text-secondary);
        }
    }

    .delete {
        color: var(--red-primary);

        &:hover {
            text-decoration: underline;
        }
    }

    input[type="radio"] {
        display: none;
    }
</style>