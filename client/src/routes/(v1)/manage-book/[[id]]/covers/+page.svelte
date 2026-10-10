<script lang="ts">
    import type { BookManagementStore } from "@root/stores/BookManagementStore.svelte.js";
    import      { api_client, logger }              from "@root/CompositionRoot.js";
    import      Button                  from "@root/components/button.svelte";
    import      ListEntryCover          from "@root/components/list-entry-cover.svelte";
    import      TopbarBack              from "@root/components/topbar-back.svelte";
    import Upload from "@root/icons/Upload.svelte";
    import Link from "@root/icons/Link.svelte";
    import Sheet from "@root/components/Sheet.svelte";
    import CoverImage from "@root/components/CoverImage.svelte";
    import { ApiError } from "@root/errors/ApiError.js";

    
    let { data } = $props()
    const { book_store } : { book_store: BookManagementStore } = $derived(data)

    let file_input: HTMLInputElement
    let sheet: ReturnType<typeof Sheet>

    async function onFileSelect()
    {
        const file = file_input.files?.[0]

        if (!file) 
        {
            logger.info("no file selected")
            return
        }

        book_store.addCover(file)
        file_input.value = ""
    }



    let url                                  = $state("")
    let preview_file: File | null            = $state(null)
    let status: "idle" | "loading" | "error" = $state("idle")
    let error_message: string | null         = $state(null)

    let debounce_timer: ReturnType<typeof setTimeout>


    $effect(() =>
    {
        clearTimeout(debounce_timer)
        preview_file = null

        if (!url.trim()) 
        { 
            status = "idle"
            return 
        }

        debounce_timer = setTimeout(async () =>
        {
            status = "loading"
            console.log("sending request")

            try
            {
                const blob = await api_client.fetchImageFromUrl(url)
                preview_file = new File([blob], "cover.jpg", { type: blob.type })

                status = "idle"
                console.log("got back image")
            }
            catch (e)
            {
                status = "error"

                if (e instanceof ApiError)
                {
                    error_message = e.message
                }
                else
                {
                    error_message = "An error occurred. Try again later."
                }
            }
        }, 500)
    })

    
    function confirm()
    {
        if (preview_file) 
        {
            book_store.addCover(preview_file)
            cancelPicker()
        }
    }


    function cancelPicker()
    {
        sheet.close()
        url = ""
        preview_file = null
        status = "idle"
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
        
        <div class = "buttons">
            <button onclick={ () => file_input.click() }>
                <Upload />
                Upload
            </button>

            <button onclick={ () => sheet.open() }>
                <Link />
                <span>Link</span>
            </button>
        </div>
    {:else}
        <div class = "buttons">
            <button onclick={ () => file_input.click() }>
                <Upload />
                Upload
            </button>

            <button onclick={ () => sheet.open() }>
                <Link />
                <span>Link</span>
            </button>
        </div>

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

<Sheet
    bind:this = { sheet }
>
    <div class="sheet-body">
        <input 
            type = "url" 
            bind:value={url} 
            placeholder = "Paste image URL..." 
            disabled = { status === "loading" }
        />

        {#if error_message !== null}
            <span class="error">{ error_message }</span>
        {/if}

        {#if preview_file !== null}
            <CoverImage source = {{ kind: "FILE",   file: preview_file }} />

            <div class = "buttons">
                <Button text = "Confirm" onClick = { confirm } />
                <Button text = "Cancel" onClick = { cancelPicker } />
            </div>
        {/if}
    </div>
</Sheet>



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

    .buttons
    {
        display: flex;
        flex-direction: row;
        gap: 0.5rem;
        padding: 0 1rem;
        align-items: center;
        align-self: stretch;

        button
        {
            display: flex;
            flex-direction: row;
            gap: 0.5rem;
            border-radius: 12px;
            padding: 12px 16px;
            display: flex;
            justify-content: center;
            flex: 0 0 auto;
            background-color: var(--raised);
            color: var(--text);
            border: 1px solid var(--raised-border);
            box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.25);
            flex: 1;
            line-height: 24px;
        }
    }

    .sheet-body
    {
        display: flex;
        flex-direction: column;
        gap: 1rem;

        input
        {
            padding: 0.25rem;
            font-size: 0.9rem;
        }

        .error
        {
            font-size: small;
            color: var(--red-primary);
        }
    }
</style>
