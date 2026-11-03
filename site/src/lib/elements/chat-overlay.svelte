<script lang="ts">
    import IconButton from "#lib/components/buttons/icon-button.svelte";
    import { ripple_effect } from "#lib/components/buttons/ripple-effect.ts";
    import Ex from "#lib/components/icons/ex.svelte";
    import Sparkles from "#lib/components/icons/sparkles.svelte";
    import Trash from "#lib/components/icons/trash.svelte";
    import MessageBar from "#lib/components/message-bar.svelte";
    import type { ChatMessage, ChatStatus } from "#lib/stores/chat-store.svelte.ts";
    import { onMount } from "svelte";


    let {
        is_chat_empty,
        messages,
        status,
        class: className,
        onSend,
        onClose,
        onReset,
    }: {
        is_chat_empty: boolean
        messages: ChatMessage[]
        status: ChatStatus
        class?: string
        onSend:  (text: string) => void
        onClose: () => void
        onReset: () => void
    } = $props()



    let md_renderer = $state((src: string) => src)
    let rendered_messages: ChatMessage[] = $derived(messages.map(it => ({ role: it.role, is_error: it.is_error, content: md_renderer(it.content) })))

    
    onMount( async () => 
    {
        const markdown = await import("#lib/utils/markdown.client.ts")
        md_renderer = markdown.renderMarkdown
    })
</script>



<chat-overlay class = {[ className ]}>
    <topbar-chat>
        <IconButton
            label   = "close chat"
            shape   = "square"
            onclick = { onClose }
        >
            <Ex />
        </IconButton>

        <h1 class = "title">Ask Sanctum AI</h1>

        {#if !is_chat_empty}
            <IconButton
                label   = "close chat"
                shape   = "square"
                onclick = { onReset }
            >
                <Trash />
            </IconButton>
        {/if}
    </topbar-chat>

    <chat-content>
        {#if is_chat_empty}
            <welcome-wrapper>
                <Sparkles class = "sparkles" width = { 60 } height = { 60 } />
                <p class = "welcome-message">What about asking...</p>

                <chips-wrapper>
                    <button 
                        class   = "chip ripple-container" 
                        onclick = { () => onSend("How do I install with Docker?") }
                        {@attach ripple_effect} 
                    >
                        How do I install with Docker?
                    </button>

                    <button 
                        class   = "chip ripple-container"
                        onclick = { () => onSend("How do I add a user?") }
                        {@attach ripple_effect} 
                    >
                        How do I add a user?
                    </button>

                    <button 
                        class = "chip ripple-container"
                        onclick = { () => onSend("What can I use Sanctum for?") }
                        {@attach ripple_effect} 
                    >
                        What can I use Sanctum for?
                    </button>
                </chips-wrapper>
            </welcome-wrapper>
        {:else}
            <message-list>
                {#each rendered_messages as message, index (index)}
                    <message-wrapper 
                        class = {[ "message", "doc-body", message.role ]}
                        class:error = { message.is_error }
                    >
                        {@html message.content}
                    </message-wrapper>
                {/each}

                {#if status === "waiting"}
                    <p class = "message assistant waiting">…</p>
                {/if}
            </message-list>
        {/if}

        <MessageBar 
            value = ""
            placeholder = "Ask a question..." 
            onSubmit = { onSend }
        />
    </chat-content>
</chat-overlay>



<style>
    chat-overlay
    {
        width: min(500px, 100vw);

        display: flex;
        flex-direction: column;

        background-color: var(--bg-surface-raised);

        box-shadow: -4px 4px 4px 0 rgba(0, 0, 0, 5%);

        height: 100dvh;
        overflow: hidden;
    }


    topbar-chat
    {
        display: flex;
        flex-direction: row;
        align-items: center;

        padding: 8px 0;

        border-bottom: 1px solid var(--border-neutral-default);
    }


    h1.title
    {
        font-size: var(--text-heading-sm);
        font-weight: 600;

        flex: 1;
    }


    chat-content
    {
        display: flex;
        flex-direction: column;

        padding: 20px;

        flex: 1;
        min-height: 0;
        overflow: hidden;
    }


    welcome-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        flex: 1;

        gap: 32px;
    }


    :global(svg.sparkles)
    {
        color: var(--text-accent-default);
    }


    p.welcome-message
    {
        color: var(--text-neutral-subtle);
        margin-bottom: -16px;
    }


    chips-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    button.chip
    {
        border: 1px solid var(--border-neutral-default);
        border-radius: 999px;

        padding: 12px 16px;
        margin: 5px 0;


        &:hover
        {
            background-color: var(--bg-overlay-subtle);
        }
    }

    message-list
    {
        display: flex;
        flex-direction: column;
        gap: 24px;

        flex: 1;
        min-height: 0;
        overflow-y: auto;
    }

    message-wrapper
    {
        padding: 12px;
        border-radius: 8px;
    }
    
    message-wrapper.user
    {
        background-color: var(--bg-accent-subtle);
        align-self: flex-end;
    }


    message-wrapper.assistant
    {
        align-self: flex-start;
    }
</style>