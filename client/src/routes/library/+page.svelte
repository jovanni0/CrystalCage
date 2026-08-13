<script lang="ts">
    import BookCoverGallery from "@components/book-cover-gallery.svelte";
    import { library_store } from "@root/CompositionRoot";
</script>



<div class="lib-body">
    {#each library_store.gallery as book (book.id)}
        <BookCoverGallery
            title    = { book.name }
            cover_id = { book.cover_id ?? undefined }
        />
    {/each}
</div>



<style>
    .lib-body {
        display: grid;
        grid-template-columns: repeat(
            auto-fill,
            minmax(120px, 130px)
        );

        gap: 16px;
        padding: 16px;

        justify-content: center;
    }
</style>