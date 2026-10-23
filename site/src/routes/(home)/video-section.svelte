<script lang="ts">
    import type { HomeContent_VideoSection } from "#lib/content/home-content.ts";


    let {
        content
    }: {
        content: HomeContent_VideoSection
    } = $props()



    let is_playing: boolean = $state(false)

    let embed_URL: string = $derived(`https://www.youtube-nocookie.com/embed/${ content.youtube_id }?autoplay=1&rel=0`)
</script>



<video-section>
    <p class = "title">{ content.title }</p>

    {#if is_playing}
        <iframe 
            src             = { embed_URL }
            title           = { content.title }
            allow           = "accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            referrerpolicy  = "strict-origin-when-cross-origin"
            allowfullscreen
        ></iframe>
    {:else}
        <button
            onclick = { () => is_playing = true }
            disabled = { content.youtube_id === "" }
        >
            <img
                src = { content.thumbnail }
                alt = { content.thumbnail_alt }
            />
        </button>
    {/if}

    <p class = "caption">{ content.caption }</p>
</video-section>



<style>
    video-section
    {
        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 64px 20px 96px 20px;
        
        text-align: center;

        p.title
        {
            font-size: var(--text-heading-md);
            font-weight: 600;
        }
        
        button,
        iframe
        {
            width: min(100%, 600px);
            height: auto;
            aspect-ratio: 16/9;

            img
            {
                width: 100%;
                height: 100%;

                border-radius: 16px;
            }
        }

        p.caption
        {
            font-size: var(--text-body);
            color: var(--text-muted);
            
            margin-top: -32px;
        }
    }
</style>