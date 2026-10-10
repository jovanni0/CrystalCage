<script lang="ts">
    import Button from "@root/components/button.svelte";
    import FormatChip from "@root/components/FormatChip.svelte";
    import RadioSelector from "@root/components/radio-selector.svelte";
    import type { DisplayEdition } from "@root/types/DisplayEdition";
    import { formatColor } from "@root/utils/formatColor";

    let { 
        editions, 
        selected_id = $bindable(null), 
        button_text,
        onConfirm 
    } : {
        editions: DisplayEdition[]
        selected_id?: string | null
        button_text: string
        onConfirm: () => void
    } = $props()

    let dialog_el: HTMLDialogElement

    export function open() { dialog_el.showModal() }
    export function close() { dialog_el.close() }
</script>



<dialog bind:this={dialog_el} class="sheet">
    <div class="handle"></div>

    <div class = "info">
        <span class = "heading">Start reading</span>
        <span class = "subheading">You have {editions.length} version{editions.length === 1 ? "" : "s"} - which are you reading?</span>
    </div>

    <ul class="edition-list">
        {#each editions as edition (edition.id)}
            <li>
                <button
                    type="button"
                    class="edition-row"
                    onclick={() => selected_id = edition.id}
                    aria-pressed={selected_id === edition.id}
                >
                    <FormatChip format={edition.format} color={formatColor(edition.format)} />
                    <div class="info">
                        <span class="label">{edition.label ?? "(Unlabeled)"}</span>
                        <span class="added">Added {edition.added_at}</span>
                    </div>
                    <RadioSelector 
                        selected = { selected_id === edition.id }
                    />
                </button>
            </li>
        {/each}
    </ul>

    <Button 
        text = { button_text }
        onClick = { onConfirm }
        fill = { true }
    />
</dialog>



<style>
    .sheet {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        top: auto;
        width: 100%;
        max-width: none;
        margin: 0;
        border: none;
        border-radius: 20px 20px 0 0;
        padding: 24px 16px;
        background: var(--base);
        box-sizing: border-box;
    }

    .sheet[open] {
        display: flex;
        flex-direction: column;
    }

    .sheet::backdrop {
        background: rgba(0, 0, 0, 0.4);
    }

    .handle {
        width: 40px;
        height: 4px;
        border-radius: 2px;
        background: var(--dividers);
        margin: 0 auto 20px;
    }

    .info
    {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;

        .heading
        {
            font-weight: 500;
        }

        .subheading
        {
            color: var(--text-secondary);
        }
    }

    .edition-list {
        list-style: none;
        margin: 16px 0;
        padding: 0;
    }

    .edition-row {
        all: unset;
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 12px;
        width: 100%;
        padding: 12px 0;
        border-bottom: 1px solid var(--dividers);
        cursor: pointer;
        box-sizing: border-box;
    }

    .info {
        flex: 1;
        display: flex;
        flex-direction: column;

        .added
        {
            font-size: small;
            color: var(--text-secondary);
        }
    }
</style>