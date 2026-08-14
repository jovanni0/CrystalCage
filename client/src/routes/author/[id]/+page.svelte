<script lang="ts">
    import type { AuthorStore }  from "@root/stores/AuthorStore.svelte";
    import      BookCoverGallery from "@components/book-cover-gallery.svelte";


    let { data } = $props()

    const { author_store }: { author_store: AuthorStore } = $derived(data)
</script>



<div class = "content">
    {#each author_store.gallery as book (book.id)}
        <BookCoverGallery
            title    = { book.title }
            cover_id = { book.cover_id ?? undefined }
            href     = { `/book/${book.id}` }
        />
    {/each}
</div>



<style>
    .content {
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