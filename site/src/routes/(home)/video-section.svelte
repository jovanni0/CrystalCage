<script lang="ts">
    import type { HomeContent_VideoSection } from "#lib/content/home-content.ts";


    let {
        content
    }: {
        content: HomeContent_VideoSection
    } = $props()
</script>



<video-section>
    <p class = "title">{ content.title }</p>

    <button>
        <img
            src = { content.thumbnail }
            alt = { content.thumbnail_alt }
        />
    </button>

    <p class = "caption">{ content.caption }</p>
</video-section>



<style>
    video-section
    {
        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 64px 0 96px 0;

        p.title
        {
            font-size: var(--text-heading-md);
            font-weight: 600;
        }

        button
        {
            width: 525px;
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