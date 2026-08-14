<script lang="ts">
    import CoverImage from "@root/components/CoverImage.svelte";
    import Star       from "@root/icons/Star.svelte";


    let {
        cover_id,
        cover_name,
        added_at,
        selected,
    } : {
        cover_id: string
        cover_name: string | null
        added_at: string
        selected: boolean
    } = $props()
</script>



<button>
    <CoverImage 
        cover_id = { cover_id }
        alt      = { cover_name ?? "book-cover" }
    />

    <div 
        class = "star"
        class:selected = { selected === true }
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
        width: 100%;
        height: 100%;
    }

    .star
    {
        position: absolute;
        top: 0.5rem;
        right: 0.5rem;
        color: var(--text-inverted);

        &.selected
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