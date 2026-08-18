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
        /* grid-template-columns: repeat(auto-fit, minmax(60px, 1fr)); */
        grid-template-columns: repeat(auto-fit, minmax(min(120px, calc((100% - 2 * 16px) / 3)), 1fr));
        column-gap: 0.5rem;
        row-gap: 1rem;
        padding: 1rem;

        justify-content: center;
    }
</style>