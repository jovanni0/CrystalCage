<script lang="ts">
    import CoverImage from "@root/components/CoverImage.svelte";
    import Star       from "@root/icons/Star.svelte";


    let {
        cover_id,
        cover_name,
        added_at,
        selected,
        onClick,
    } : {
        cover_id: string
        cover_name: string | null
        added_at: string
        selected: boolean,
        onClick?: (() => void | Promise<void>) | undefined
    } = $props()
</script>



<button onclick = {onClick}>
    <CoverImage 
        source = {{ kind: "REMOTE", id: cover_id}}
        alt    = { cover_name ?? "book-cover" }
    />

    <div 
        class = "star"
        class:selected = { selected }
    >
        <Star />
    </div>

    <div class = "info">
        <span class = "title">{ cover_name }</span>
        <span class = "added-at">{ added_at }</span>
    </div>
</button>



<style>
    button
    {
        position: relative;
        display: flex;
        flex-direction: column;
    }

    .star
    {
        position: absolute;
        top: 0.5rem;
        right: 0.5rem;
        color: var(--text-inverted);

        &.selected :global(svg)
        {
            fill: currentColor;
        }
    }

    .info
    {
        display: flex;
        flex-direction: column;
        padding: 0.5rem 0.75rem;
        font-size: 0.9rem;
        text-align: center;
    }

    .title
    {
        font-weight: 500;
    }

    .added-at
    {
        color: var(--text-secondary);
    }
</style>