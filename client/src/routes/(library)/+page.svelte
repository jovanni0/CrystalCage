<script lang="ts">
    import { library_store } from "@root/CompositionRoot";
    import BookCoverGallery  from "@components/book-cover-gallery.svelte";
    import Button            from "@root/components/button.svelte";
    import CategorySeparator from "@root/components/category-separator.svelte";
</script>



<div 
    class = "page"
    class:empty = { library_store.is_library_empty }
    class:gallery-layout = { library_store.view === "GALLERY" }
>
    {#if library_store.is_library_empty }
        <span>Your library is empty.<br/>Add your first book to get started.</span>

        <Button 
            text="Add book" 
            href="/manage-book"
        />

    {:else if library_store.view === "GALLERY"}
        {#each library_store.gallery as book (book.id)}
            <BookCoverGallery
                title    = { book.name }
                cover_id = { book.cover_id ?? undefined }
                href     = { `/book/${book.id}` }
            />
        {/each}

    {:else if library_store.view === "AUTHOR"}
        {#each library_store.by_author as group (group.author_name)}
            <div class="group">
                <CategorySeparator 
                    title = { group.author_name }
                    href  = { `/author/${group.author_id}` }
                />

                <div class="gallery-layout">
                    {#each group.items as book (book.id)}
                        <BookCoverGallery 
                            title    = { book.name }
                            cover_id = { book.cover_id ?? undefined}
                            href     = { `/book/${book.id}`}
                        />
                    {/each}
                </div>
            </div>
        {/each}
        
    {:else}
        {#each library_store.by_series as group (group.author_name+group.universe_name)}
            <div class="group">
                <CategorySeparator 
                    title    = { group.author_name }
                    subtitle = { group.universe_name }
                    href     = { `/author/${group.author_id}` }
                />

                <div class="gallery-layout">
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
    {/if}
</div>



<style>
    .page
    {
        flex: 1;
        min-height: 0;
        overflow-y: auto;
        overscroll-behavior-y: contain;
        scrollbar-width: none;

        display: flex;
        flex-direction: column;
        padding: 16px;

        & > :not(:first-child) {
            border-top: 1px solid var(--dividers);
            padding-top: 8px;
        }
    }

    .page.empty
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        text-align: center;
        gap: 1rem;
    }
    
    .gallery-layout
    {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
        column-gap: 0.5rem;
        row-gap: 1rem;
        padding: 1rem;
        justify-content: center;
    }

    .group 
    {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }
</style>