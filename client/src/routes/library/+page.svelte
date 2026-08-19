<script lang="ts">
    import BookCoverGallery from "@components/book-cover-gallery.svelte";
    import { library_store } from "@root/CompositionRoot";
</script>



<div class="lib-body">
    {#each library_store.gallery as book (book.id)}
        <BookCoverGallery
            title    = { book.name }
            cover_id = { book.cover_id ?? undefined }
            href     = { `/book/${book.id}` }
        />
    {/each}
</div>



<style>
    .lib-body {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
        column-gap: 0.5rem;
        row-gap: 1rem;
        padding: 1rem;
        justify-content: center;

        & > *
        {
            width: 100%;
            max-width: calc((100% - 2 * 0.5rem) / 3);
        }
    }
</style>