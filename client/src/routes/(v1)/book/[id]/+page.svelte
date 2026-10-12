<script lang="ts">
    import type { BookStore }     from "@root/stores/BookStore.svelte";
    import      BookPresentation  from "./BookPresentation.svelte";
    import      StatRow           from "@root/components/StatRow.svelte";
    import      TextBlock         from "@root/components/TextBlock.svelte";


    let { data } = $props()

    const { book_store }: { book_store: BookStore } = $derived(data)
</script>



<div class = "content">
    <BookPresentation 
        author_id     = { book_store.author_id }
        author_name   = { book_store.author_name }
        universe_name = { book_store.universe_name }
        series_name   = { book_store.series_name }
        volume_number = { book_store.volume_number }
        read_status   = { book_store.read_status }
        read_count    = { book_store.read_count }
        cover_id      = { book_store.cover_id }
    />

    <StatRow 
        stats = {[ 
            { label: "release", value: book_store.launch_date ?? "(Unset)" },
            { label: "words", value: book_store.word_count?.toString() ?? "(Unset)" },
            { label: "reads", value: book_store.read_count.toString() },
        ]}
    />

    {#if book_store.notes}
        <TextBlock 
            title    = "my opinion"
            content  = { book_store.notes }
            highlite = { true }
        />
    {/if}

    {#if book_store.blurb}
        <TextBlock
            title    = "blub"
            content  = { book_store.blurb }
            highlite = { false }
        />
    {/if}
</div>



<style>
    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;
    }
</style>