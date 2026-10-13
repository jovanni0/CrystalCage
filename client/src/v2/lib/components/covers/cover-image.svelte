<script lang="ts">
    import type { CoverFrameFit, 
                  CoverFrameImageSize, 
                  CoverFrameStatus }    from "./cover-frame.svelte";
    import type { CoverType }           from "@v2/lib/clients/api-covers";
    import      { fullsize_cover_cache, 
                  thumbnail_cover_cache, 
                  visibility_observer } from "@root/CompositionRoot";
    import      CoverFrame              from "./cover-frame.svelte";



    let {
        cover_id,
        cover_type,
        alt                      = "",
        fit                      = "cover",
        is_lazy                  = true,
        should_preview_thumbnail = false,
        onImageLoad,
    }: {
        cover_id:                  string | null
        cover_type:                CoverType
        alt?:                      string
        fit?:                      CoverFrameFit
        is_lazy?:                  boolean
        should_preview_thumbnail?: boolean
        onImageLoad?:              (size: CoverFrameImageSize) => void
    } = $props()


    let status: CoverFrameStatus = $state("loading")
    let src:    string | null    = $state(null)

    // A parent that rebuilds its item objects re-sends the same values through new objects; a derived only notifies its
    // readers when the value itself changes, so the cover is not reloaded (and its <img> not recreated) for nothing.
    const stable_cover_id:                 string | null = $derived(cover_id)
    const stable_cover_type:               CoverType     = $derived(cover_type)
    const stable_is_lazy:                  boolean       = $derived(is_lazy)
    const stable_should_preview_thumbnail: boolean       = $derived(should_preview_thumbnail)


    function loadCover(node: HTMLElement): (() => void) | void
    {
        const current_id  = stable_cover_id
        const cover_cache = stable_cover_type === "original" ? fullsize_cover_cache : thumbnail_cover_cache

        src = null

        if (current_id === null)
        {
            status = "empty"
            return
        }

        let object_url:  string | null = null
        let is_canceled: boolean       = false
        let is_observed: boolean       = false


        function show(blob: Blob): void
        {
            const previous_url = object_url

            object_url = URL.createObjectURL(blob)
            src = object_url
            status = "loaded"

            if (previous_url !== null)
            {
                URL.revokeObjectURL(previous_url)
            }
        }


        async function fetchAndShow(): Promise<void>
        {
            if (!current_id) return
            
            const result = await cover_cache.load(current_id)

            if (is_canceled) return

            // Keep the thumbnail if possible. A visible thumbnail preview is better than an error icon.
            if (!result.ok && object_url === null)
            {
                status = "error"
                return
            }

            if (result.ok)
            {
                show(result.value)
            }
        }


        function cleanup()
        {
            is_canceled = true

            if (is_observed)
            {
                visibility_observer.unobserve(node)
            }

            if (object_url !== null)
            {
                URL.revokeObjectURL(object_url)
            }
        }


        const cached = cover_cache.peek(current_id)

        if (cached !== null)
        {
            show(cached)
            
            return cleanup
        }

        status = "loading"

        if (stable_should_preview_thumbnail && stable_cover_type === "original" && stable_cover_id)
        {
            const thumbnail = thumbnail_cover_cache.peek(stable_cover_id)

            if (thumbnail !== null)
            {
                show(thumbnail)
            }
        }

        if (stable_is_lazy)
        {
            is_observed = true
            visibility_observer.observe(node, fetchAndShow)
        }
        else
        {
            fetchAndShow()
        }

        return cleanup
    }
</script>



<CoverFrame 
    status       = { status }
    src          = { src }
    alt          = { alt }
    fit          = { fit }
    onImageLoad  = { onImageLoad }
    onImageError = { () => status = "error" }
    {@attach loadCover}
/>