<script lang="ts">
    import type { TagManagementStore } from '@root/stores/TagMangementStore.svelte.js';
    import      TopbarConfirmCancel    from '@root/components/topbar-confirm-cancel.svelte';
    import      BookLinkListItem       from '../../BookLinkListItem.svelte';
    import      { markdown }           from '@codemirror/lang-markdown';
    import      { placeholder }        from '@codemirror/view';
    import      { goBack }             from '@root/utils/go-back.js';
    import      { onMount }            from 'svelte';
    import      { EditorView, minimalSetup } from 'codemirror';
    import Button from '@root/components/button.svelte';


    let { data } = $props()
    const { 
        tag_store, 
        id 
    } : { 
        tag_store: TagManagementStore
        id: string | undefined 
    } = $derived(data)

    let editor_el: HTMLDivElement
    let view: EditorView

    onMount( () => 
    {
        view = new EditorView({
            doc: tag_store.description ?? undefined,
            extensions: [
                minimalSetup,
                markdown(),
                placeholder("Description..."),
                EditorView.updateListener.of( (update) =>
                {
                    if (update.docChanged)
                    {
                        tag_store.description = update.state.doc.toString()
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


    async function save(): Promise<void>
    {
        const is_saved = await tag_store.save()

        if (is_saved)
        {
            goBack()
        }
    }


    async function deleteTag(): Promise<void>
    {
        const is_deleted = await tag_store.delete()

        if (is_deleted)
        {
            goBack()
        }
    }
</script>



<TopbarConfirmCancel 
    title     = { id ? tag_store.name ?? "" : "Add Tag" }
    onCancel  = { goBack }
    onConfirm = { save }
/>

<div class = "content">
    <div class = "inputs">
        <input 
            class       = "name"
            placeholder = "Tag name..."
            bind:value=   { tag_store.name }
            required
        >

        <div class="editor" bind:this = { editor_el }></div>

        {#if tag_store.error_message !== null}
            <span class = "error">{ tag_store.error_message }</span>
        {/if}
    </div>

    <div class = "links">
        <span class = "usage">{ tag_store.link_count > 0 ? `Used by ${ tag_store.link_count } books` : `Not used by any books yet` }</span>

        <ul class = "books">
            {#each tag_store.book_linked as link (link.book_id)}
                <BookLinkListItem 
                    title = { link.book_title }
                    series = { link.series_name }
                    cover_id = { link.cover_id }
                    onClick  = { id ? () => tag_store.removeLink(link.book_id) : undefined }
                />
            {/each}
        </ul>
    </div>

    {#if id}
        <Button 
            text    = "Delete"
            type    = "secondary"
            onClick = { deleteTag }
        />
    {/if}
</div>



<style>
    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;
    }

    .inputs
    {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    input {
        all: unset;
        background-color: var(--input-fill);
        padding: 0.9rem;
        border-radius: 0.5rem;
        color: var(--text);
        border: 1px solid var(--lowered-border);
    }

    .editor {
        min-height: 8rem;
        background-color: var(--input-fill);
        border-radius: 8px;
        border: 1px solid var(--lowered-border);
        isolation: isolate;
        padding: 8px;
    }

    .editor :global(.cm-editor.cm-focused) {
        outline: none;
    }

    .error
    {
        color: var(--red-primary);
        font-size: small;
        padding: 0 8px;
    }

    .links
    {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .usage
    {
        padding-left: 0.5rem;
        color: var(--text-secondary);
    }

    .books
    {
        list-style: none;
        margin: 0;
        padding: 0;
    }
</style>