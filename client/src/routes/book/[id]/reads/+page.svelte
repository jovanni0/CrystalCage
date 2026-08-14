<script lang="ts">
    import type { BookStore } from "@root/stores/BookStore.svelte";
    import      ReadListItem  from "./ReadListItem.svelte";

    
    let { data } = $props()

    const { book_store }: { book_store: BookStore } = $derived(data)
</script>



<div class = "content">
    {#each book_store.read_history as read, index (read.id)}
        <ReadListItem 
            read_number      = { index }
            start_date       = { read.start_date ?? "Unknown" }
            end_date         = { read.end_date ?? "Unknown" }
            notes            = { read.notes }
            edition_format   = { read.edition_format }
            edition_label    = { read.edition_format }
            edition_added_at = { read.edition_release_date }
        />
    {/each}
</div>



<style>
    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        padding: 16px;
    }
</style>