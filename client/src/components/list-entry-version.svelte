<script lang="ts">
    import ChipFormat from "./chip-format.svelte";

    let {
        format,
        input_value,
        created_at,
        onChange,
        onDelete,
    } : {
        format: string
        input_value?: string
        created_at: Date
        onChange?: (value: string) => void
        onDelete?: () => void
    } = $props()
</script>



<div class="version-entry">
    <div class="top">
        <input 
            class="value"
            class:none-set={input_value === undefined || input_value === ""}
            class:epub={format.toLowerCase() === "epub"}
            class:mobi={format.toLowerCase() === "mobi"}
            class:fvb={format.toLowerCase() === "fvb"}
            placeholder="(No title set)"
            onchange={ (e) => onChange?.(e.currentTarget.value) }
            value={input_value}
            required
        >

        <span class="date faded">Added at {created_at.toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric"})}</span>
    </div>

    <!-- <span class="not-read faded">This version has not been read yet.</span> -->
    
    <div class="bottom">
        <ChipFormat format={format} />
        <button
            class="delete"
            onclick={onDelete}
        >
            Delete version
        </button>
    </div>
</div>



<style>
    .version-entry {
        display: flex;
        flex-direction: column;
        gap: 20px;
        border-bottom: 1px solid var(--dividers);
        padding: 24px 0;
        align-self: stretch;
    }

    .top {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    .date {
        padding: 0 8px;
    }

    .bottom {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        align-items: center;
        padding: 0 8px;
    }

    .not-read {
        padding: 0 16px;
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
        font-weight: 500;

        &.none-set {
            font-style: italic;
            color: var(--text-secondary);
        }

        &.epub { color: var(--peach-text); }

        &.mobi { color: var(--mauve-text);}

        &.fvb { color: var(--teal-text);}
    }

    .delete {
        color: var(--red-primary);

        &:hover {
            text-decoration: underline;
        }
    }
</style>