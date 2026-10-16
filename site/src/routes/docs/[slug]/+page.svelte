<script lang="ts">
    import TableOfContents from '#lib/components/table-of-contents.svelte';

    let { data } = $props()
</script>



<svelte:head>
    <title>{ data.doc.meta.title } · Sanctum</title>
    <meta name = "description" content = { data.doc.meta.description } />
</svelte:head>

<doc-layout>
    <TableOfContents
        headings = { data.doc.headings }
    />

    <doc-size-limiter>
        <doc-body>
            <h1>{ data.doc.meta.title }</h1>
            {@html data.doc.html}
        </doc-body>
    </doc-size-limiter>
</doc-layout>



<style>
    doc-layout
    {
        display: grid;
        grid-template-columns: 20rem minmax(0, 1fr) 20rem;

        doc-size-limiter
        {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin: 64px 32px;
            container-type: inline-size;
        }
    }
</style>