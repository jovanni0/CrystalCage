<script lang="ts">
    import type { BookManagementStore } from "@root/stores/BookManagementStore.svelte.js";
    import      Button                  from "@root/components/button.svelte";
    import      ListEntryVersion        from "@root/components/list-entry-version.svelte";
    import      TopbarBack              from "@root/components/topbar-back.svelte";


    let { data } = $props()
    const { book_store } : { book_store: BookManagementStore } = $derived(data)
    let file_input: HTMLInputElement


    async function onFileSelect()
    {
        const file = file_input.files?.[0]

        if (!file) return

        book_store.addEdition(file)
        file_input.value = ""
    }
</script>



<TopbarBack 
    page_name = "Manage Editions"
/>

<div class="body">
    <input 
        type="file"
        accept=".epub,.mobi,.azw3,.fvb,application/epub+zip"
        bind:this={file_input}
        onchange={onFileSelect}
        style="display: none"
    />

    {#if !book_store.has_editions}
        <div class="info">
            <span class="main">No files added yet</span>
            <span>When you have a copy — an EPUB, MOBI, or FVB — add it here as a version. You can have multiple versions of the same book and choose which one you're reading each time.</span>
        </div>
        
        <Button 
            text="Add edition" 
            type="primary" 
            onClick={ () => file_input.click() }
        />
    {:else}
        <Button 
            text="Add version" 
            type="secondary" 
            onClick={ () => file_input.click() }
        />

        <div class="editions">
            {#each book_store.editions as edition (edition.id)}
                <ListEntryVersion 
                    input_value = { edition.label ?? undefined }
                    format      = { edition.format }
                    created_at  = { edition.added_at !== null ? new Date(edition.added_at) : new Date() }
                    onChange    = { (text) => book_store.setEditionLabel(edition.id, text) }
                    onDelete    = { () => book_store.removeEdition(edition.id) }
                />
            {/each}
        </div>
    {/if}
</div>



<style>
    .body {
        display: flex;
        flex-direction: column;
        align-items: center;
        padding: 16px;
    }

    .info {
        display: flex;
        flex-direction: column;
        gap: 12px;
        color: var(--text-secondary);
        letter-spacing: 0.05rem;
        text-align: center;
        padding: 32px 8px;
        padding-top: 64px;
    }

    .main {
        color: var(--text);
    }

    .editions {
        display: flex;
        flex-direction: column;
        align-self: stretch;
    }
</style>