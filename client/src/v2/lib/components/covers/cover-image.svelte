<script lang="ts">
    import type { CoverFrameStatus }    from "./cover-frame.svelte";
    import      { fullsize_cover_cache, 
                  thumbnail_cover_cache, 
                  visibility_observer } from "@root/CompositionRoot";
    import      CoverFrame              from "./cover-frame.svelte";
    import type { CoverType } from "@v2/lib/clients/api-covers";



    let {
        cover_id,
        cover_type,
        alt = "",
    }: {
        cover_id:   string | null
        cover_type: CoverType
        alt?:       string
    } = $props()


    let status: CoverFrameStatus = $state("loading")
    let src:    string | null    = $state(null)


    function loadCover(node: HTMLElement): (() => void) | void
    {
        const current_id = cover_id
        const cover_cache = cover_type === "original" ? fullsize_cover_cache : thumbnail_cover_cache
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
            
            const result = await cover_cache.load(current_id)

            if (is_canceled) return

            if (!result.ok)
            {
                status = "error"
                return
            }

            show(result.value)
        }

        const cached = cover_cache.peek(current_id)

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