<script lang="ts">
    import FloatingActions from '#lib/elements/floating-actions.svelte';
    import TableOfContents from '#lib/elements/table-of-contents.svelte';

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
    
    <FloatingActions />
</doc-layout>




<style>
    doc-layout
    {
        display: grid;
        grid-template-columns: 20rem minmax(0, 1fr) 20rem;

        @media (width <= 1500px)
        {
            grid-template-columns: 20rem minmax(0, 1fr) auto;
        }

        @media (width <= 1200px)
        {
            grid-template-columns: 27vw minmax(0, 1fr) auto;
        }

        @media (width <= 800px)
        {
            grid-template-columns: 1fr;
            grid-template-rows: auto 1fr;
        }


        doc-size-limiter
        {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 64px 32px;
            container-type: inline-size;


            @media (width <= 800px)
            {
                padding: 16px 32px 48px 32px;
            }


            @media (width <= 500px)
            {
                padding: 16px 20px 48px 20px;
            }
        }
    }
</style>