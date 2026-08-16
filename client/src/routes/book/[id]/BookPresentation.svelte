<script lang="ts">
    import type { ReadStatus }      from "@root/types/ReadStatus";
    import      CoverImage          from "@root/components/CoverImage.svelte";
    import      ReadStatusIndicator from "@root/components/ReadStatusIndicator.svelte";


    let {
        author_id,
        author_name,
        universe_name,
        series_name,
        volume_number,
        read_status,
        read_count,
        cover_id,
    } : {
        author_id:     string
        author_name:   string
        universe_name: string
        series_name:   string
        volume_number: number | null
        read_status:   ReadStatus
        read_count:    number
        cover_id:      string | null
    } = $props()
</script>


<div class = "presentation">
    <CoverImage 
        source = { cover_id ? { kind: "REMOTE", id: cover_id} : null }
        alt    = "book-cover"
        width  = "160px"
    />

    <div class="info">
        <span class = "author">
            by 
            <a href = { `/author/${author_id}` }>{ author_name }</a>
        </span>

        <div class = "secondary">
            <span class = "series">{ `${series_name} #${volume_number}` }</span>
            <span class = "universe">{ universe_name }</span>
        </div>

        <ReadStatusIndicator 
            status     = { read_status }
            read_count = { read_count }
        />
    </div>
</div>



<style>
    .presentation
    {
        display: inline-flex;
        gap: 1.5rem;
    }
    
    .info
    {
        display: flex;
        flex-direction: column;
        padding: 0.75rem 0;
        justify-content: space-around;
    }

    .secondary
    {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }
    
    .author
    {
        font-weight: 500;

        a
        {
            font-size: large;
            font-weight: 600;
            color: var(--primary-text);
        }
    }

    .universe
    {
        color: var(--text-secondary);
    }
</style>