
<script lang="ts">
    import type { ReadManagementStore }      from "@root/stores/ReadManagementStore.svelte.js";
    import      { markdown }                 from "@codemirror/lang-markdown";
    import      { EditorView, minimalSetup } from "codemirror";
    import      { onMount }                  from "svelte";
    import      TopbarBack                   from "@root/components/topbar-back.svelte";


    let { data } = $props()
    const { read_store } : { read_store: ReadManagementStore } = $derived(data)

    let editor_el: HTMLDivElement
    let view: EditorView


    onMount( () => 
    {
        view = new EditorView({
            doc: read_store.notes ?? undefined,
            extensions: [
                minimalSetup,
                markdown(),
                EditorView.updateListener.of( (update) =>
                {
                    if (update.docChanged)
                    {
                        read_store.notes = update.state.doc.toString()
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
    page_name = "Notes"
/>

<div class="body">
    <div class="editor" bind:this={editor_el}></div>
</div>



<style>
    .body {
        display: flex;
        flex-direction: column;
        gap: 20px;
        padding: 16px;
        flex: 1;
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