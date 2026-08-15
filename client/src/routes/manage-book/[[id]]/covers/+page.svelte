<script lang="ts">
    import type { BookManagementStore } from "@root/stores/BookManagementStore.svelte.js";
    import      Button                  from "@root/components/button.svelte";
    import      ListEntryCover          from "@root/components/list-entry-cover.svelte";
    import      TopbarBack              from "@root/components/topbar-back.svelte";

    
    let { data } = $props()
    const { book_store } : { book_store: BookManagementStore } = $derived(data)

    let file_input: HTMLInputElement


    async function onFileSelect()
    {
        const file = file_input.files?.[0]

        if (!file) return

        book_store.addCover(file)
        file_input.value = ""
    }
</script>



<TopbarBack 
    page_name = "Manage Covers"
/>

<div class="body">
    <input 
        type = "file"
        accept = "image/*"
        bind:this = { file_input }
        onchange = { onFileSelect }
        style = "display: none"
    />

    {#if book_store.cover_count === 0}
        <div class="info">
            <span class="main">No covers added yet.</span>
            <span>Adding multiple covers lets you choose how this book appears in your library.</span>
        </div>
        
        <Button 
            text="Add cover" 
            type="primary" 
            onClick={ () => file_input.click() }
        />
    {:else}
        <Button 
            text="Add cover" 
            type="secondary" 
            onClick={ () => file_input.click() }
        />

        <div class="covers">
            {#each book_store.covers as cover (cover.id)}
                <ListEntryCover
                    name         = "cover"
                    input_value  = { cover.label ?? undefined }
                    image_source = { cover.image_source }
                    radio_value  = { cover.id }
                    checked      = { cover.is_default }
                    onChange     = { (text) => book_store.setCoverLabel(cover.id, text) }
                    onDelete     = { () => book_store.removeCover(cover.id) }
                    onSelected   = { () => book_store.setDefaultCoverId(cover.id) }
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
        gap: 16px;
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

    .covers {
        display: flex;
        flex-direction: column;
    }
</style>
