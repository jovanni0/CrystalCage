<script lang="ts">
    import type { CoverImageStatus } from "@root/types/CoverImageStatus";
    import type { CoverSource }      from "@root/types/CoverSource";
    import      { api_client }       from "@root/CompositionRoot";
    import      { lazyVisible }      from "@root/utils/LazyVisible";
    import      BookOpen             from "./icons/book-open.svelte";


    let {
        source,
        alt,
        width,
    } : {
        source: CoverSource | null
        alt?:   string | undefined
        width?: string | undefined
    } = $props()


    let image_url: string | null    = $state(null)
    let status:    CoverImageStatus = $state("IDLE")


    async function load()
    {
        if (source === null || status != "IDLE")
        {
            return
        }

        status = "LOADING"

        try
        {
            if (source.kind === "FILE")
            {
                image_url = URL.createObjectURL(source.file)
                status = "LOADED"
            }
            else
            {
                const blob = await api_client.getCoverBlob(source.id)

                image_url = URL.createObjectURL(blob)
                status = "LOADED"
            }
        }
        catch
        {
            status = "ERROR"
        }
    }


    $effect( () =>
    {
        return () => {
            if (image_url)
            {
                URL.revokeObjectURL(image_url)
            }
        }
    })
</script>



<div 
    class           = "cover-observer" 
    use:lazyVisible = { load }
    style:--width   = { width }
>
    {#if status === "LOADED"}
        <img 
            src = { image_url } 
            alt = { alt } 
        />
    {:else if status === "ERROR"}
        <div 
            class = "cover-fallback"
        ></div>
    {:else}
        <div 
            class = "image-placeholder"
        >
            <BookOpen 
                width={32} 
                height={32}
            />
        </div>
    {/if}
</div>



<style>
    .cover-observer
    {
        aspect-ratio: 2/3;
        border-radius: 8px;
        overflow: hidden;
        width: var(--width);
        height: fit-content;
    }
    
    img 
    {
        aspect-ratio: 2/3;
        object-fit: cover;
        width: 100%;
    }

    .image-placeholder 
    {
        width: 100%;
        height: 100%;
        min-width: 120px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background-color: var(--placeholder);
        color: var(--text-secondary);
    }
</style>