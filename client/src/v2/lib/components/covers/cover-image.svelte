<script lang="ts">
    import type { CoverFrameStatus }    from "./cover-frame.svelte";
    import      { cover_image_cache, 
                  visibility_observer } from "@root/CompositionRoot";
    import      CoverFrame              from "./cover-frame.svelte";



    let {
        cover_id,
        alt = "",
    }: {
        cover_id: string | null
        alt?:     string
    } = $props()


    let status: CoverFrameStatus = $state("loading")
    let src:    string | null    = $state(null)


    function loadCover(node: HTMLElement): (() => void) | void
    {
        const current_id = cover_id
        src = null

        if (current_id === null)
        {
            status = "empty"
            return
        }

        let object_url:  string | null = null
        let is_canceled: boolean       = false


        function show(blob: Blob)
        {
            object_url = URL.createObjectURL(blob)
            src = object_url
            status = "loaded"
        }


        async function fetchAndShow()
        {
            if (!current_id) return
            
            const result = await cover_image_cache.load(current_id)

            if (is_canceled) return

            if (!result.ok)
            {
                status = "error"
                return
            }

            show(result.value)
        }

        const cached = cover_image_cache.peek(current_id)

        if (cached !== null)
        {
            show(cached)
        }
        else
        {
            status = "loading"
            visibility_observer.observe(node, fetchAndShow)
        }

        return () =>
        {
            is_canceled = true
            visibility_observer.unobserve(node)

            if (object_url !== null)
            {
                URL.revokeObjectURL(object_url)
            }
        }
    }
</script>



<CoverFrame 
    status       = { status }
    src          = { src }
    alt          = { alt }
    onImageError = { () => status = "error" }
    {@attach loadCover}
/>