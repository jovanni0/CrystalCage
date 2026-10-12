<script lang="ts">
    import type { EditionManagerStore } from "@v2/lib/stores/edition-manager.store.svelte"
    import      { onDestroy }           from "svelte"
    import      Page                    from "@v2/lib/components/_other/page.svelte"
    import      TopbarBack              from "@v2/lib/components/bars/topbar-back.svelte"
    import      Button                  from "@v2/lib/components/buttons/button.svelte"



    let { data } = $props()

    const store: EditionManagerStore = $derived(data.store)

    let file_input: HTMLInputElement


    function onFilesPicked()
    {
        if (file_input.files)
        {
            store.addFiles(file_input.files)
        }

        file_input.value = ""
    }


    onDestroy(() => store.dispose())
</script>



<Page>
    {#snippet topbar()}
        <TopbarBack title = "Manage editions" />
    {/snippet}

    <manager>
        <input
            type      = "file"
            accept    = ".epub,.mobi,.azw3,.fvb,.pdf,application/epub+zip,application/pdf"
            multiple
            hidden
            bind:this = { file_input }
            onchange  = { onFilesPicked }
        />

        <actions>
            <Button size = "slim" onclick = { () => file_input.click() }>Pick files</Button>
        </actions>

        {#if store.error_message}
            <p class = "error">{ store.error_message }</p>
        {/if}


        {#if store.has_drafts}
            <h2>Not uploaded yet</h2>

            <list>
                {#each store.drafts as draft (draft.local_id)}
                    <row>
                        <fields>
                            <small>{ draft.file.name }</small>

                            <input
                                placeholder = "label (optional)"
                                bind:value={ draft.label }
                                disabled    = { draft.status === "uploading" }
                            />

                            <input
                                placeholder = "format"
                                bind:value={ draft.format }
                                disabled    = { draft.status === "uploading" }
                            />

                            {#if draft.status === "uploading"}
                                <small>Uploading…</small>
                            {:else if draft.error}
                                <small class = "error">{ draft.error }</small>
                            {/if}
                        </fields>

                        <button
                            disabled = { draft.status === "uploading" }
                            onclick  = { () => store.removeDraft(draft.local_id) }
                        >
                            Remove
                        </button>
                    </row>
                {/each}
            </list>

            <Button
                state   = { store.is_uploading ? "loading" : "default" }
                onclick = { () => store.uploadDrafts() }
            >
                Upload { store.drafts.length } { store.drafts.length === 1 ? "edition" : "editions" }
            </Button>
        {/if}


        <h2>On the server</h2>

        {#if !store.has_editions}
            <p class = "muted">No editions yet. Pick an EPUB, MOBI or other book file above.</p>
        {:else}
            <list>
                {#each store.editions as edition (edition.id)}
                    <row>
                        <fields>
                            <input
                                placeholder = "label (optional)"
                                value       = { edition.label ?? "" }
                                onchange    = { (event) => store.renameEdition(edition, event.currentTarget.value) }
                            />

                            <small>{ edition.format.toUpperCase() } · added { edition.added_at.slice(0, 10) }</small>
                        </fields>

                        <button onclick = { () => store.deleteEdition(edition) }>Delete</button>
                    </row>
                {/each}
            </list>
        {/if}
    </manager>
</Page>



<style>
    manager
    {
        display: flex;
        flex-direction: column;
        gap: 12px;
        padding: 16px;
    }

    actions
    {
        display: flex;
        gap: 8px;
    }

    list
    {
        display: flex;
        flex-direction: column;
        gap: 8px;
    }

    row
    {
        display: flex;
        gap: 12px;
        align-items: center;
        padding: 8px 0;
        border-bottom: 1px solid var(--text-placeholder);
    }

    fields
    {
        display: flex;
        flex-direction: column;
        gap: 6px;
        flex: 1;
        min-width: 0;

        input
        {
            padding: 6px;
        }
    }

    h2
    {
        font-size: var(--text-body);
        margin: 8px 0 0;
    }

    .error
    {
        color: var(--text-error);
    }

    .muted
    {
        color: var(--text-muted);
    }
</style>
