<script lang="ts">
    import type { CoverImageStatus } from "@root/types/CoverImageStatus";
    import      { api_client }       from "@root/CompositionRoot";
    import      { lazyVisible }      from "@root/utils/LazyVisible";

    let {
        cover_id,
        alt = ""
    } : {
        cover_id: string,
        alt: string
    } = $props()

    let image_url: string | null = $state(null)
    let status: CoverImageStatus = $state(cover_id === null ? "ERROR" : "IDLE")


    async function load()
    {
        if (cover_id === null || status != "IDLE")
        {
            return
        }

        status = "LOADING"

        try
        {
            const blob = await api_client.getCoverBlob(cover_id)
            image_url = URL.createObjectURL(blob)
            status = "LOADED"
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
    class="cover" 
    use:lazyVisible={load}
>
    {#if status === "LOADED"}
        <img src={image_url} {alt} />
    {:else if status === "ERROR"}
        <div class="cover-fallback"></div>
    {:else}
        <div class="cover-skeleton"></div>
    {/if}
</div>



<style>
    img {
        width: 100%;
        aspect-ratio: 2/3;
    }
</style>