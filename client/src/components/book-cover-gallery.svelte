<script lang="ts">
    import CoverImage from "./CoverImage.svelte";


    let {
        title,
        cover_id,
        href,
    } : {
        title:     string
        cover_id?: string | undefined
        href:      string
    } = $props()
</script>



<div class="gallery-cover">
    <a href  = { href } >
        <CoverImage
            source = { cover_id ? { kind: "REMOTE", id: cover_id} : null }
            alt    = { title }
        />
    </a>

    <span>{title}</span>
</div>



<style>
    .gallery-cover 
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
    }

    span 
    {
        word-break: break-word;
        text-align: center;
    }
</style>