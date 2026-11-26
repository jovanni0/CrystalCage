<script lang="ts">
    import type { CoverFrameFit,
                  CoverFrameStatus } from "./cover-frame.svelte";
    import      CoverFrame           from "./cover-frame.svelte";



    let {
        file,
        alt = "",
        fit = "cover",
    }: {
        file: Blob
        alt?: string
        fit?: CoverFrameFit
    } = $props()


    let status: CoverFrameStatus = $state("loading")
    let src:    string | null    = $state(null)


    $effect(() =>
    {
        const object_url = URL.createObjectURL(file)

        src    = object_url
        status = "loaded"

        return () =>
        {
            URL.revokeObjectURL(object_url)
        }
    })
</script>



<CoverFrame
    { status }
    { src }
    { alt }
    { fit }
    onImageError = { () => status = "error" }
/>