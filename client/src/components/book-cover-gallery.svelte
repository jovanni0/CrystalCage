<script lang="ts">
    // import { BASE_URL } from "$lib/api/author";
    import CoverPlaceholder from "./cover-placeholder.svelte";
    import CoverImage from "./CoverImage.svelte";

    let {
        title,
        cover_id
    } : {
        title: string
        cover_id?: string
    } = $props()
</script>



<div class="gallery-cover">
    <div class="cover-image">
        {#if cover_id}
            <CoverImage
                cover_id = { cover_id }
                alt = "cover-image"
            />
        {:else}
            <CoverPlaceholder />
        {/if}
    </div>

    <span>{title}</span>
</div>



<style>
    .gallery-cover {
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
    }

    .cover-image {
        width: 100%;
        height: 100%;
        aspect-ratio: 2/3;
        object-fit: cover;
        border-radius: 8px;
        overflow: hidden;
    }

    span {
        word-break: break-word;
        text-align: center;
    }
</style>