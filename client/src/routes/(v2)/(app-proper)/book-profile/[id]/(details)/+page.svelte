<script lang="ts">
    import type { BookProfileStore } from "@v2/lib/stores/book-profile-store.svelte";
    import      StatRow from "@v2/lib/components/_other/stat-row.svelte";
    import      CoverImage from "@v2/lib/components/covers/cover-image.svelte";
    import      Galaxy from "@v2/lib/components/icons/galaxy.svelte";
    import      Library from "@v2/lib/components/icons/library.svelte";
    import      User from "@v2/lib/components/icons/user.svelte";
    import      GenericPill from "@v2/lib/components/pills/generic-pill.svelte";
    import CoverTile from "@v2/lib/components/covers/cover-tile.svelte";



    let {
        data
    }: {
        data: { book_profile_store: BookProfileStore }
    } = $props()

    
    const store = $derived(data.book_profile_store)
    const stats = $derived([
        { label: "words",   value: store.word_count ?? "" },
        { label: "release", value: store.released_at ?? "" },
        { label: "reads",   value: store.read_count },
    ])
</script>



<page-wrapper>
    <cover-info-wrapper>
        <cover-wrapper>
            <CoverTile>
                <CoverImage 
                    cover_id   = { store.cover_id }
                    cover_type = "thumbnail"
                    alt        = { store.title }
                />
            </CoverTile>
        </cover-wrapper>

        <info-wrapper>
            <p class = "title">{ store.title }</p>

            <grid-wrapper>
                <User />
                <p class = "author">{ store.author }</p>

                {#if store.universe}
                    <Galaxy />
                    <p class = "universe">{ store.universe }</p>
                {/if}

                {#if store.series}
                    <Library />
                    <p class = "series">{ store.series }</p>
                {/if}
            </grid-wrapper>
        </info-wrapper>
    </cover-info-wrapper>

    <StatRow stats = { stats } />

    {#if store.tags.length > 0}
        <tags-wrapper>
            {#each store.tags as tag (tag)}
                <GenericPill label = { tag } />
            {/each}
        </tags-wrapper>
    {/if}

    {#if store.thoughts}
        <category-wrapper>
            <p class = "category-heading">THOUGHTS</p>

            <thoughts-wrapper>
                {@html store.thoughts}
            </thoughts-wrapper>
        </category-wrapper>
    {/if}

    {#if store.blurb}
        <category-wrapper>
            <p class = "category-heading">BLURB</p>

            <blurb-wrapper>
                {@html store.blurb}
            </blurb-wrapper>
        </category-wrapper>
    {/if}
</page-wrapper>



<style>
    page-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
    }

    cover-info-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 16px;

        cover-wrapper
        {
            width: 112px;
            height: auto;
            aspect-ratio: 2/3;
            flex: 0 0 112px;
        }

        info-wrapper
        {
            display: flex;
            flex-direction: column;
            gap: 8px;
            flex: 1;
            padding: 16px 0;

            grid-wrapper
            {
                display: grid;
                grid-template-columns: auto 1fr;
                align-items: center;
                gap: 8px;

                p.author,
                p.universe,
                p.series
                {
                    color: var(--text-muted);
                    font-size: var(--text-body-sm);
                }

                & > :global(svg)
                {
                    color: var(--text-brand-secondary);
                }
            }
        }
    }

    tags-wrapper
    {
        display: flex;
        flex-direction: row;
        flex-wrap: wrap;
        gap: 4px;
    }

    category-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 8px;

        p.category-heading
        {
            color: var(--text-brand-secondary);
            letter-spacing: 1.2;
            font-weight: 600;
        }
    
        thoughts-wrapper
        {
            background-color: rgba(0, 0, 0, 5%);
            padding: 8px;
            border-left: 4px solid var(--text-placeholder);
            text-align: justify;
        }

        blurb-wrapper
        {
            padding: 0 8px 0 12px;
            text-align: justify;
        }
    }

</style>