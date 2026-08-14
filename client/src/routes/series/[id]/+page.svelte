<script lang="ts">
    import SeriesListItem from "@root/components/SeriesListItem.svelte";
    import TopbarBack     from "@root/components/topbar-back.svelte";

    let { data } = $props()
    const { series_store } = data
</script>


<TopbarBack 
    page_name = {series_store.series_name}
/>
<span class = "author-indicator">
    by 
    <a 
        href  = { series_store.author_id !== null ? `/author/${series_store.author_id}` : "error"}
        class = "author-link"
    >{ series_store.author_name }</a>
</span>
<lu class = "content">
    {#each series_store.books as book (book.id)}
        <SeriesListItem
            book_id    = { book.id }
            cover_id   = { book.cover_id }
            title      = { book.title }
            date       = { book.launch_date }
            words      = { book.word_count }
            status     = { book.read_status }
            read_count = { book.reread_count }
        />
    {/each}
</lu>


<style>
    .content
    {
        display: flex;
        flex-direction: column;
        padding: 16px;
        margin: 0;
    }

    .author-indicator
    {
        text-align: center;

        a
        {
            color: var(--primary-text);
            text-decoration: underline;
        }
    }
</style>