<script lang="ts">
    import type { BookStore }     from '@root/stores/BookStore.svelte';
    import      BookCoverSelector from './BookCoverSelector.svelte';


    let { data } = $props()

    const { book_store }: { book_store: BookStore } = $derived(data)
</script>



<div class = "content">
    {#each book_store.covers as cover (cover.id)}
        <BookCoverSelector 
            cover_id    = { cover.id }
            cover_name  = { cover.label }
            added_at    = { cover.added_at }
            selected    = { cover.is_default }
        />
    {/each}
</div>




<style>
    .content
    {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 32px;
        padding: 16px;
    }
</style>