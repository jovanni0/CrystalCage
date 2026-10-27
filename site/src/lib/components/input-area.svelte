<script lang="ts">
    import      { markdown }     from "@codemirror/lang-markdown";
    import      { EditorView, 
                  minimalSetup } from "codemirror";
    import      { onMount }      from "svelte";



    let {
        value        = $bindable(),
        preallocate  = false,
        is_mandatory = false,
        label,
        error,
        class: className,
    }: {
        value?:        string
        error?:        string
        preallocate?:  boolean
        is_mandatory?: boolean
        label:         string
        class?:        string
    } = $props()


    let area_wrapper: HTMLDivElement
    let editor_view:  EditorView


    onMount( () => 
    {
        editor_view = new EditorView({
            doc: value ?? undefined,
            extensions: [
                minimalSetup,
                markdown(),
                EditorView.updateListener.of( (update) =>
                {
                    if (update.docChanged)
                    {
                        value = update.state.doc.toString().trim()
                    }
                }),
                EditorView.theme({
                    '.cm-scroller': { overflow: 'visible' },
                    '.cm-content': { fontFamily: '"Manrope", sans-serif', padding: '0' }
                }),
                EditorView.lineWrapping,
            ],
            parent: area_wrapper
        })

        return () => editor_view.destroy()
    })
</script>



<input-area class = { className }>
    <fieldset>
        <legend>
            { label }

            {#if is_mandatory}
                <span class = "mandatory-mark">*</span>
            {/if}
        </legend>

        <area-wrapper bind:this = { area_wrapper }></area-wrapper>
    </fieldset>
    
    {#if preallocate || error}
        <p class = "error-message">{ error }</p>
    {/if}
</input-area>



<style>
    input-area
    {
        display: grid;
        grid-template-rows: 1fr auto;
        align-self: stretch;

        gap: 2px;
    }


    fieldset
    {
        all: unset;

        position: relative;

        display: grid;
        grid-template-rows: 1fr;

        border: 1px solid var(--text-neutral-default);
        border-radius: 16px;

        padding: 4px 16px 12px 16px;
        margin: 0 1px 1px 1px;


        &:has(:global(.cm-editor.cm-focused))
        {
            border: 2px solid var(--text-brand-default);
            margin: 0px;
        }
    }


    area-wrapper
    {
        display: grid;
        min-height: 0;

        margin: 0 -2px 0 -6px; /* offsets CodeMirror line padding. */

        :global(.cm-editor.cm-focused)
        {
            outline: none;
        }
    }



    legend
    {
        font-size: var(--text-body-sm);
        color: var(--text-neutral-default);
    }   


    p.error-message
    {
        font-size: var(--text-body-sm);
        color: var(--text-danger-default);
        padding-left: 16px;
        min-height: 1.2rem;
    }


    span.mandatory-mark
    {
        color: var(--text-danger-default);
    }
</style>