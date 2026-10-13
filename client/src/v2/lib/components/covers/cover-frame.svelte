<script lang="ts" module>
    export type CoverFrameStatus = "empty" | "loading" | "loaded" | "error"
    export type CoverFrameFit = "cover" | "contain"

    export type CoverFrameImageSize = { 
        width: number, 
        height: number 
    }
</script>



<script lang="ts">
    import type { HTMLAttributes } from "svelte/elements";
    import      BookOpenText       from "../icons/book-open-text.svelte";
    import      ImageLoadingError  from "../icons/image-loading-error.svelte";



    let {
        status,
        src = null,
        alt = "",
        fit = "cover",
        onImageLoad,
        onImageError,
        ...rest
    }: {
        status:        CoverFrameStatus
        src?:          string | null
        alt?:          string | null
        fit?:          CoverFrameFit
        onImageLoad?:  (size: CoverFrameImageSize) => void
        onImageError?: () => void
    } & HTMLAttributes<HTMLElement> = $props()


    function reportSize(event: Event): void
    {
        if (onImageLoad === undefined) return

        const image = event.currentTarget as HTMLImageElement

        onImageLoad({
            width: image.naturalWidth,
            height: image.naturalHeight
        })
    }
</script>



<cover-frame
    class:loading = { status === "loading" }
    class:error   = { status === "error" }
    class:empty   = { status === "empty" }
    { ...rest }
>
    {#if status === "loaded" && src !== null}
        <img
            src           = { src }
            alt           = { alt }
            decoding      = "async"
            draggable     = "false"
            class:contain = { fit === "contain" }
            onload        = { reportSize }
            onerror       = { onImageError }
        />
    {:else if status === "error"}
        <ImageLoadingError />
        <p>Error loading image</p>
    {:else if status === "empty"}
        <BookOpenText />
    {/if}
</cover-frame>



<style>
    cover-frame
    {
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 4px;

        width: 100%; 
        height: 100%; 

        text-align: center;
        line-height: 1;
        font-size: var(--text-body-sm);
        color: var(--text-placeholder);

        &.error
        {
            color: var(--text-error-muted);
        }

        &.loading
        {
            background: linear-gradient(
                150deg,
                rgba(0, 0, 0, 0.20) 0%,
                rgba(255, 255, 255, 0.1) 50%,
                rgba(0, 0, 0, 0.20) 100%
            );
            background-size: 200% 200%;
            animation: shimmer 1.5s linear infinite;
        }

        &.empty 
        {
            background-color: rgba(0, 0, 0, 10%);
        }

        & > :global(svg)
        {
            width: 65%;
            max-width: 80px;
            height: auto;
            aspect-ratio: 1;
        }

        & > :global(svg path)
        {
            vector-effect: non-scaling-stroke;
        }
    }


    img
    {
        width: 100%;
        height: 100%;
        object-fit: cover;
        user-select: none;
        -webkit-user-drag: none;

        &.contain
        {
            object-fit: contain;
        }
    }


    @keyframes shimmer 
    {
        from 
        {
            background-position: 100% 0;
        }

        to 
        {
            background-position: 0% 100%;
        }
    }
</style>