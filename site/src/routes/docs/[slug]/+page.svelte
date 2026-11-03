<script lang="ts">
    import ChatOverlay from '#lib/elements/chat-overlay.svelte';
    import FloatingActions from '#lib/elements/floating-actions.svelte';
    import TableOfContents from '#lib/elements/table-of-contents.svelte';
    import { chat_store } from '#lib/stores/chat-store.svelte.js';

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
    
    <FloatingActions onOpenChat = { () => chat_store.is_open = true } />

    {#if chat_store.is_open}
        <ChatOverlay
            is_chat_empty = { chat_store.is_empty }
            messages      = { chat_store.messages }
            status        = { chat_store.status }
            class         = "chat"
            onClose       = { () => chat_store.is_open = false }
            onReset       = { () => chat_store.reset() }
            onSend        = { (text) => chat_store.send(text) }
        />
    {/if}
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

    :global(chat-overlay.chat)
    {
        position: fixed;
        top: 0;
        bottom: 0;
        right: 0;

        z-index: 100;
    }
</style>