<script lang="ts">
    import type { CoverManagerStore } from "@v2/lib/stores/cover-manager.store.svelte"
    import      { onDestroy }         from "svelte"
    import      CoverImage            from "@root/components/CoverImage.svelte"
    import      Page                  from "@v2/lib/components/_other/page.svelte"
    import      TopbarBack            from "@v2/lib/components/bars/topbar-back.svelte"
    import      Button                from "@v2/lib/components/buttons/button.svelte"



    let { data } = $props()

    const store: CoverManagerStore = $derived(data.store)

    let file_input: HTMLInputElement
    let url = $state("")


    function onFilesPicked()
    {
        if (file_input.files)
        {
            store.addFiles(file_input.files)
        }

        file_input.value = ""
    }


    async function addFromUrl()
    {
        const is_added = await store.addFromUrl(url)

        if (is_added)
        {
            url = ""
        }
    }


    onDestroy(() => store.dispose())
</script>



<Page>
    {#snippet topbar()}
        <TopbarBack title = "Manage covers" />
    {/snippet}

    <manager>
        <input
            type      = "file"
            accept    = "image/*"
            multiple
            hidden
            bind:this = { file_input }
            onchange  = { onFilesPicked }
        />

        <actions>
            <Button size = "slim" onclick = { () => file_input.click() }>Pick images</Button>
        </actions>

        <url-row>
            <input
                type        = "url"
                placeholder = "or paste an image link"
                bind:value={ url }
                disabled    = { store.is_fetching }
            />
            <Button
                size    = "slim"
                variant = "ghost"
                state   = { store.is_fetching ? "loading" : url.trim() ? "default" : "disabled" }
                onclick = { addFromUrl }
            >
                Add
            </Button>
        </url-row>

        {#if store.error_message}
            <p class = "error">{ store.error_message }</p>
        {/if}


        {#if store.has_drafts}
            <h2>Not uploaded yet</h2>

            <list>
                {#each store.drafts as draft (draft.local_id)}
                    <row>
                        <img src = { draft.preview_url } alt = "" />

                        <fields>
                            <input
                                placeholder = "label (optional)"
                                bind:value={ draft.label }
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
                Upload { store.drafts.length } { store.drafts.length === 1 ? "cover" : "covers" }
            </Button>
        {/if}


        <h2>On the server</h2>

        {#if !store.has_covers}
            <p class = "muted">No covers yet. Pick images or paste a link above.</p>
        {:else}
            <list>
                {#each store.covers as cover (cover.id)}
                    <row>
                        <CoverImage source = {{ kind: "REMOTE", id: cover.id }} width = "64px" />

                        <fields>
                            <input
                                placeholder = "label (optional)"
                                value       = { cover.label ?? "" }
                                onchange    = { (event) => store.renameCover(cover, event.currentTarget.value) }
                            />

                            <label>
                                <input
                                    type     = "radio"
                                    name     = "default-cover"
                                    checked  = { cover.is_default }
                                    onchange = { () => store.setDefault(cover.id) }
                                />
                                Default
                            </label>
                        </fields>

                        <button onclick = { () => store.deleteCover(cover) }>Delete</button>
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

    actions, url-row
    {
        display: flex;
        gap: 8px;
        align-items: center;
    }

    url-row input
    {
        flex: 1;
        min-width: 0;
        padding: 8px;
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

    row img
    {
        width: 64px;
        height: 96px;
        object-fit: cover;
    }

    fields
    {
        display: flex;
        flex-direction: column;
        gap: 6px;
        flex: 1;
        min-width: 0;

        input:not([type = "radio"])
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
