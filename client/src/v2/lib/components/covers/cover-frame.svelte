<script lang="ts" module>
    export type CoverFrameStatus = "empty" | "loading" | "loaded" | "error"
</script>



<script lang="ts">
    import type { HTMLAttributes } from "svelte/elements";
    import      BookOpenText       from "../icons/book-open-text.svelte";
    import      ImageLoadingError  from "../icons/image-loading-error.svelte";



    let {
        status,
        src = null,
        alt = "",
        onImageError,
        ...rest
    }: {
        status:        CoverFrameStatus
        src?:          string | null
        alt?:          string | null
        onImageError?: () => void
    } & HTMLAttributes<HTMLElement> = $props()
</script>



<cover-frame
    class:loading = { status === "loading" }
    class:error   = { status === "error" }
    class:empty   = { status === "empty" }
    { ...rest }
>
    {#if status === "loaded" && src !== null}
        <img
            src = { src }
            alt = { alt }
            decoding = "async"
            onerror  = { onImageError }
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
        overflow: hiddden;

        width: 100%; 
        aspect-ratio: 2/3;
        gap: 4px;

        border: 1px solid var(--text-placeholder);
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 0 2px 0 rgba(0, 0, 0, 0.2);

        text-align: center;
        line-height: 1;
        font-size: var(--text-body-small);
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

        &.empty > :global(svg)
        {
            width: 65%;
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