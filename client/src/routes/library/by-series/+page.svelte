<script lang="ts">
    import CategorySeparator from "@components/category-separator.svelte";
    import BookCoverGallery  from "@components/book-cover-gallery.svelte";
    import { library_store } from "@root/CompositionRoot";
</script>



<div class="lib-body">
    {#each library_store.by_series as group (group.author_name+group.universe_name)}
        <div class="group">
            <CategorySeparator 
                title    = { group.author_name }
                subtitle = { group.universe_name }
                href     = { `/author/${group.author_id}` }
            />

            <div class="covers">
                {#each group.items as series (series.id)}
                    <BookCoverGallery 
                        title    = { series.name }
                        cover_id = { series.cover_id ?? undefined }
                        href     = { `/series/${series.id}` }
                    />
                {/each}
            </div>
        </div>
    {/each}
</div>



<style>
    .lib-body {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;

        & > :not(:first-child) {
            border-top: 1px solid var(--dividers);
            padding-top: 8px;
        }
    }

    .group {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .covers {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        column-gap: 0.5rem;
        row-gap: 1rem;
        justify-content: center;

        & > :global(*)
        {
            width: 100%;
            max-width: calc((100% - 2 * 0.5rem) / 3);
        }
    }
</style>