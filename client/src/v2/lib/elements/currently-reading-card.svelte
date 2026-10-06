<script lang="ts">
    import ProgressBar  from "../components/_other/progress-bar.svelte";
    import CoverImage   from "../components/covers/cover-image.svelte";
    import ChevronRight from "../components/icons/chevron-right.svelte";


    let {
        title,
        author,
        book_id,
        cover_id,
        progress,
    }: {
        title: string
        author: string
        book_id: string
        cover_id: string | null
        progress: number
    } = $props()
</script>



<a
    href = "/book/{book_id}"
>
    <cover-wrapper>
        <CoverImage 
            cover_id   = { cover_id }
            cover_type = "thumbnail"
            alt        = { title }
        />
    </cover-wrapper>

    <details-wrapper>
        <info-wrapper>
            <p class = "title">{ title }</p>
            <p class = "author">by { author }</p>
        </info-wrapper>

        <ProgressBar percentage = { progress } />
    </details-wrapper>

    <ChevronRight />
</a>



<style>
    a
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;
        padding: 8px 32px 8px 40px;
        border-radius: 8px;

        color: inherit;
        text-decoration: none;
        -webkit-tap-highlight-color: transparent;
    }

    cover-wrapper
    {
        width: 50px;
        aspect-ratio: 2/3;
    }

    details-wrapper
    {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-width: 0;
        gap: 16px;
        padding-left: 12px;
    }

    info-wrapper
    {
        display: flex;
        flex-direction: column;
    }

    p.title
    {
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;

        overflow-wrap: anywhere;
        line-height: 1.2;
    }

    p.author
    {
        color: var(--text-muted);
        font-size: var(--text-body-sm);
    }
</style>