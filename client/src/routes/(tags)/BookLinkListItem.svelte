<script lang="ts">
    import CoverImage       from "@root/components/CoverImage.svelte";
    import CircleX          from "@root/icons/CircleX.svelte";


    let {
        title,
        series,
        cover_id,
        onClick = undefined
    } : {
        title:    string
        series:   string | null
        cover_id: string | null
        onClick?: () => void
    } = $props()
</script>



<li class = "book-link-list-item">
    <CoverImage 
        source = { cover_id ? { kind: "REMOTE", id: cover_id} : null }
        alt    = { title }
        width  = "80px"
    />

    <div class = "details">
        <span class = "title">{ title }</span>

        {#if series}
            <span class = "series">{ series }</span>
        {/if}
    </div>

    {#if onClick !== undefined }
        <button onclick = {onClick}>
            <CircleX />
        </button>
    {/if}
</li>



<style>
    .book-link-list-item
    {
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid var(--dividers);
        padding: 0.5rem;
        list-style: none;
        gap: 0.75rem;
        align-items: center;
    }

    .details
    {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
        flex: 1;
    }

    .series
    {
        color: var(--text-secondary);
    }

    button
    {
        padding: 0.5rem;
        color: var(--text-secondary);
    }
</style>