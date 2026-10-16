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

    <!-- <doc-toc>
        <p>On this page</p>
        <ul>
            {#each data.doc.headings as heading (heading.id)}
                <li class:nested = { heading.depth === 3 }>
                    <a href = "#{ heading.id }">{ heading.text }</a>
                </li>
            {/each}
        </ul>
    </doc-toc> -->

    <doc-body>
        <h1>{ data.doc.meta.title }</h1>
        {@html data.doc.html}
    </doc-body>
</doc-layout>