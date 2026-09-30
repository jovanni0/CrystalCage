<script lang="ts">
    import { formatColor } from "@root/utils/formatColor";
    import FormatChip from "./FormatChip.svelte";


    let {
        key = "Key",
        options,
        selected_id = $bindable(null)
    }: {
        key?: string
        options: { id: string, label: string | null, format: string }[]
        selected_id?: string | null
    } = $props()
</script>

<div class="entry">
    <span class="key">{key}</span>

    <select 
        bind:value={ selected_id } 
        class:none-set={ selected_id === null }
    >
        <button type="button"><selectedcontent></selectedcontent></button>

        {#each options as option (option.id)}
            <option value={option.id}>
                <span class="label">{option.label ?? "(Unlabeled)"}</span>
                <FormatChip
                    format = { option.format.toUpperCase() }
                    color  = { formatColor(option.format) }
                />
            </option>
        {/each}
    </select>
</div>

<style>
    .entry {
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid var(--dividers);
        padding: 12px 8px;
        padding-right: 0;
        align-items: center;
    }

    .key {
        min-width: 128px;
    }

    @supports (appearance: base-select) {
        select {
            appearance: base-select;
            flex: 1;
            border-width: 0;
            border-radius: 4px;
            background-color: var(--input-fill);
            color: var(--text);

            & selectedcontent {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 8px;
                padding: 4px 8px;
            }
        }

        select.none-set selectedcontent {
            font-style: italic;
            color: var(--text-secondary);
        }

        ::picker(select) {
            appearance: base-select;
            border: 1px solid var(--dividers);
            border-radius: 8px;
            padding: 4px;
            background-color: var(--raised);
        }

        option {
            display: flex;
            flex-direction: row;
            align-items: center;
            gap: 8px;
            padding: 8px;
            border-radius: 6px;
            

            &::checkmark {
                display: none;
            }

            &:hover {
                background-color: var(--raised);
            }
        }

        .label {
            flex: 1;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    }
</style>