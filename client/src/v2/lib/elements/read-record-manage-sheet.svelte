<script lang="ts">
    import { onMount } from "svelte";
    import BottomSheet from "../components/_other/bottom-sheet.svelte";
    import CheckableMessage from "../components/_other/checkable-message.svelte";
    import Button from "../components/buttons/button.svelte";
    import ChevronDown from "../components/icons/chevron-down.svelte";
    import DateListItem from "../components/list-items/date-list-item.svelte";
    import FormatPill from "../components/pills/format-pill.svelte";
    import { DateRules } from "../rules/date-rules";
    import type { EditionFileFormatDefinition } from "../utils/format-color-registry.svelte";
    import { placeholder }  from "@codemirror/view";
    import { markdown }     from "@codemirror/lang-markdown";
    import { EditorView, 
             minimalSetup } from "codemirror";

    let {
        is_open = $bindable(),
        start_date = $bindable(),
        end_date = $bindable(),
        edition_id = $bindable(),
        notes = $bindable(),
        is_historical = $bindable(),
        is_dnf = $bindable(),
        edition_options,
        onAction,
    }: {
        is_open:        boolean
        start_date:     string
        end_date:       string
        edition_id:     string | null
        notes:          string
        is_historical:  boolean
        is_dnf:         boolean
        edition_options: { id: string, label: string, format: EditionFileFormatDefinition }[]
        onAction: () => void
    } = $props()

    let editor_el: HTMLDivElement
    let view: EditorView

    const is_start_date_required: boolean = $derived(!is_historical)
    const is_start_date_filled:   boolean = $derived(start_date.length === 10 && DateRules.isISOdateValid(start_date))
    const is_end_date_required:   boolean = $derived(is_dnf && !is_historical)
    const is_end_date_filled:     boolean = $derived(end_date.length === 10 && DateRules.isISOdateValid(end_date))
    const can_save:           boolean = $derived((!is_start_date_required || is_start_date_filled) && (!is_end_date_required || is_end_date_filled))

    onMount( () => 
    {
        view = new EditorView({
            doc: notes,
            extensions: [
                minimalSetup,
                markdown(),
                EditorView.updateListener.of( (update) =>
                {
                    if (update.docChanged)
                    {
                        notes = update.state.doc.toString()
                    }
                }),
                EditorView.theme({
                    '&': { height: '100%' },
                    '.cm-scroller': { overflow: 'visible' },
                    '.cm-content': { fontFamily: '"Inter", sans-serif' }
                }),
                EditorView.lineWrapping,
                placeholder("You can add notes about the read here...")
            ],
            parent: editor_el
        })

        return () => view.destroy()
    })

    $effect(() =>
    {
        if (view === undefined)
        {
            return
        }

        const current = view.state.doc.toString()

        if (notes !== current)
        {
            view.dispatch({ changes: { from: 0, to: current.length, insert: notes } })
        }
    })
</script>



<BottomSheet bind:is_open>
    <DateListItem 
        key           = "Start date"
        bind:ISO_date = { start_date }
        is_disabled   = { is_historical }
        is_required   = { is_start_date_required }
    />
    <DateListItem 
        key           = "End date"
        bind:ISO_date = { end_date }
        is_disabled   = { is_historical }
        is_required   = { is_end_date_required }
    />

    <key-value-list-item>
        <p class = "key">Edition</p>
        
        <select bind:value={edition_id}>
            <button class = "select-button">
                <selectedcontent></selectedcontent>
                <ChevronDown class = "chevron" width = { 16 } height = { 16 } />
            </button>
        
            <option value = { null }>No edition</option>
            {#each edition_options as option (option.id)}
                <option value = { option.id }>
                    <p class = "option-label">{ option.label }</p>
                    <FormatPill label = { option.format.label } hue = { option.format.hue } chroma = { option.format.chroma } />
                </option>
            {/each}
        </select>
    </key-value-list-item>

    <editor-wrapper bind:this={editor_el}></editor-wrapper>

    <CheckableMessage 
        message = "Did not finish (DNF)"
        bind:is_checked = { is_dnf }
    />

    <CheckableMessage 
        message = "Is historical"
        bind:is_checked = { is_historical }
    />

    {#snippet footer()}
        <Button
            state = { can_save ? "default" : "disabled"}
            onclick = { onAction }
        >
            Record read
        </Button>
    {/snippet}
</BottomSheet>


<style>
    editor-wrapper
    {
        background-color: var(--input-fill);
        border-radius: 8px;
        isolation: isolate;
        padding: 8px;
        margin: 16px 0;

        :global(.cm-editor.cm-focused)
        {
            outline: none;
        }
    }

    key-value-list-item
    {
        display: flex;
        flex-direction: row;
        gap: 16px;
        justify-content: space-between;
        padding: 8px 8px;
        border-bottom: 1px solid var(--text-placeholder);
        align-items: center;
    }

    p.key
    {
        flex-shrink: 0;
    }

    select,
    ::picker(select)
    {
        appearance: base-select;
    }


    select
    {
        padding: 4px 16px;
        min-height: 40px;
        min-width: 0;

        border: none;
        border-radius: 8;
        background: none;
        outline: none;

        font: inherit;
        font-size: var(--text-body);
        color: inherit;
        cursor: pointer;

        background-color: rgba(0, 0, 0, 0.05);


        &::picker-icon
        {
            display: none;
        }
    }


    :global(svg.chevron)
    {
        transition: transform 0.5s;
    }


    selectedcontent
    {
        flex: 1;
    }


    ::picker(select)
    {
        background-color: var(--raised);

        border: 1px solid var(--border-neutral-default);
        border-radius: 16px;

        box-shadow: 4px 4px 4px 0 rgba(0, 0, 0, 0.2);

        margin: 8px 0;
    }


    option
    {
        min-height: 48px;
        padding: 0 16px;


        &::checkmark
        {
            display: none;
        }
    }

    selectedcontent
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;
    }

    button.select-button
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;
    }
</style>