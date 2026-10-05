<script lang="ts">
    import      { markdown }                 from "@codemirror/lang-markdown";
    import      { EditorView, minimalSetup } from "codemirror";
    import      { onMount }                  from "svelte";
    import Page from "@v2/lib/components/_other/page.svelte"
    import TopbarBack from "@v2/lib/components/topbars/topbar-back.svelte";
    import { placeholder } from "@codemirror/view";

    
    let { 
        data 
    } = $props()

    const book_store = $derived(data.book_store)

    let editor_el: HTMLDivElement
    let view:      EditorView

    
    onMount( () => 
    {
        view = new EditorView({
            doc: book_store.blurb ?? undefined,
            extensions: [
                minimalSetup,
                markdown(),
                EditorView.updateListener.of( (update) =>
                {
                    if (update.docChanged)
                    {
                        const content = update.state.doc.toString().trim()

                        book_store.blurb = content !== "" ? content : null
                    }
                }),
                EditorView.theme({
                    '&': { height: '100%' },
                    '.cm-scroller': { overflow: 'visible' },
                    '.cm-content': { fontFamily: '"Inter", sans-serif' }
                }),
                EditorView.lineWrapping,
                placeholder("The back cover summary of the book.")
            ],
            parent: editor_el
        })

        return () => view.destroy()
    })
</script>



<Page
    class = "page-content"
>
    {#snippet topbar()}
        <TopbarBack title = "Blurb" />
    {/snippet}

    <editor-wrapper bind:this={editor_el}></editor-wrapper>
</Page>



<style>
    :global(.page-content)
    {
        display: flex;
        flex-direction: column;
        padding: 16px;
        gap: 40px;
    }

    editor-wrapper
    {
        height: 100%;
        background-color: var(--input-fill);
        border-radius: 8px;
        border: 1px solid var(--lowered-border);
        isolation: isolate;
        padding: 8px;

        :global(.cm-editor.cm-focused)
        {
            outline: none;
        }
    }
</style>