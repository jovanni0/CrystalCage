<script lang="ts">
    import { BASE_URL } from "$lib/api/author";
    import CoverPlaceholder from "./cover-placeholder.svelte";

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
            <img 
                src={`${BASE_URL}/covers/${cover_id}`} 
                alt="cover" 
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

    img {
        width: 100%;
        aspect-ratio: 2/3;
    }

    span {
        word-break: break-word;
        text-align: center;
    }
</style>