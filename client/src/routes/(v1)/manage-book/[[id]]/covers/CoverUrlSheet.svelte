<script lang="ts">
    import { api_client } from "@root/CompositionRoot";
    import Sheet from "@root/components/Sheet.svelte";



    let { 
        onConfirm 
    } : {
        onConfirm: (file: File) => void
    } = $props()

    let url                                  = $state("")
    let preview_file: File | null            = $state(null)
    let status: "idle" | "loading" | "error" = $state("idle")

    let debounce_timer: ReturnType<typeof setTimeout>


    $effect(() =>
    {
        clearTimeout(debounce_timer)
        preview_file = null

        if (!url.trim()) { status = "idle"; return }

        debounce_timer = setTimeout(async () =>
        {
            status = "loading"
            try
            {
                preview_file = await fetchPreview(url)
                status = "idle"
            }
            catch
            {
                status = "error"
            }
        }, 500)
    })

    function confirm()
    {
        if (preview_file) 
        {
            onConfirm(preview_file)
        }
    }


    async function fetchPreview(url: string): Promise<File>
    {
        const blob = await api_client.fetchImageFromUrl({ url: url })
        return new File([blob], "cover.jpg", { type: blob.type })
    }
</script>





<Sheet>
    
</Sheet>



<style>

</style>