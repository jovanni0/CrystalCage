<script lang="ts">
    import type { AuthorStore }   from "@root/stores/AuthorStore.svelte";
    import      BookCoverGallery  from "@components/book-cover-gallery.svelte";
    import      CategorySeparator from "@root/components/category-separator.svelte";


    let { data } = $props()

    const { author_store }: { author_store: AuthorStore } = $derived(data)
</script>



<div class = "content">
    {#each author_store.by_series as group (group.id)}
        <div class="group">
            <CategorySeparator 
                title={group.title}
            />

            <div class="covers">
                {#each group.items as book (book.id)}
                    <BookCoverGallery 
                        title    = { book.name }
                        cover_id = { book.cover_id ?? undefined}
                        href     = { `/book/${book.id}` }
                    />
                {/each}
            </div>
        </div>
    {/each}
</div>



<style>
    .content {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;

        & > :not(:first-child) {
            border-top: 1px solid var(--dividers);
            padding-top: 8px;
        }
    }

    .group 
    {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .covers 
    {
        display: grid;
        grid-template-columns: repeat(
            auto-fill,
            minmax(120px, 130px)
        );
        gap: 16px;
        justify-content: center;
    }
</style>