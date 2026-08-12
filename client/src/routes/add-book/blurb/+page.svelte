<script lang="ts">
    import { markdown }                 from "@codemirror/lang-markdown";
    import TopbarBack                   from "@root/components/topbar-back.svelte";
    import { manage_book_store }        from "@root/CompositionRoot";
    import { EditorView, minimalSetup } from "codemirror";
    import { onMount }                  from "svelte";


    let editor_el: HTMLDivElement
    let view: EditorView

    
    onMount( () => 
    {
        view = new EditorView({
            doc: manage_book_store.blurb ?? undefined,
            extensions: [
                minimalSetup,
                markdown(),
                EditorView.updateListener.of( (update) =>
                {
                    if (update.docChanged)
                    {
                        manage_book_store.blurb = update.state.doc.toString()
                    }
                }),
                EditorView.theme({
                    '&': { height: '100%' },
                    '.cm-scroller': { overflow: 'visible' },
                    '.cm-content': { fontFamily: '"Inter", sans-serif' }
                }),
                EditorView.lineWrapping
            ],
            parent: editor_el
        })

        return () => view.destroy()
    })
</script>



<TopbarBack 
    page_name = "Blurb"
/>

<div class="body">
    <div class="wrapper">
        <span>The back of the cover blurb or your own summary.</span>
        <div class="editor" bind:this={editor_el}></div>
    </div>
</div>



<style>
    .body {
        display: flex;
        flex-direction: column;
        gap: 20px;
        padding: 16px;
        flex: 1;
    }
    
    .wrapper {
        display: flex;
        flex-direction: column;
        gap: 12px;
        min-height: 100%;
    }

    span {
        color: var(--text-secondary);
        text-align: center;
    }

    .editor {
        min-height: 50%;
        background-color: var(--input-fill);
        border-radius: 8px;
        border: 1px solid var(--lowered-border);
        isolation: isolate;
        padding: 8px;
    }

    .editor :global(.cm-editor.cm-focused) {
        outline: none;
    }
</style>