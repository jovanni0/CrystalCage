<script lang="ts">
    import Button                from "@root/components/button.svelte";
    import ListEntryCover        from "@root/components/list-entry-cover.svelte";
    import TopbarBack            from "@root/components/topbar-back.svelte";
    import { manage_book_store } from "@root/CompositionRoot";



    let file_input: HTMLInputElement


    async function onFileSelect()
    {
        const file = file_input.files?.[0]

        if (!file) return

        manage_book_store.addCover(file)
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

    {#if manage_book_store.cover_count === 0}
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
            {#each manage_book_store.covers as cover (cover.local_id)}
                <ListEntryCover
                    name        = "cover"
                    input_value = { cover.title ?? undefined }
                    cover_url   = { cover.preview_url }
                    radio_value = { cover.local_id }
                    checked     = { manage_book_store.default_cover_id === cover.local_id }
                    onChange    = { (text) => manage_book_store.setCoverTitle(cover.local_id, text) }
                    onDelete    = { () => manage_book_store.removeCover(cover.local_id) }
                    onSelected  = { () => manage_book_store.setDefaultCoverId(cover.local_id) }
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
