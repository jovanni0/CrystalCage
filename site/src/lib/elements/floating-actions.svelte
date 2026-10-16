<script lang="ts">
    import { afterNavigate } from "$app/navigation"
    import { fade, slide }          from "svelte/transition"
    import Button            from "#lib/components/buttons/button.svelte"
    import IconButton        from "#lib/components/buttons/icon-button.svelte"
    import ArrowDown         from "#lib/components/icons/arrow-down.svelte"
    import ArrowUp           from "#lib/components/icons/arrow-up.svelte"
    import Sparkles          from "#lib/components/icons/sparkles.svelte"



    // within this distance from the top/bottom of the page, the matching arrow is hidden
    const EDGE_THRESHOLD = 200



    let is_near_top:    boolean = $state(true)
    let is_near_bottom: boolean = $state(true)



    function updateScrollPosition()
    {
        const page_height = document.documentElement.scrollHeight

        is_near_top    = window.scrollY < EDGE_THRESHOLD
        is_near_bottom = window.innerHeight + window.scrollY > page_height - EDGE_THRESHOLD
    }


    function scrollToTop()
    {
        window.scrollTo({ top: 0 })
    }


    function scrollToBottom()
    {
        window.scrollTo({ top: document.documentElement.scrollHeight })
    }


    afterNavigate(() =>
    {
        updateScrollPosition()
    })
</script>



<svelte:window
    onscroll = { updateScrollPosition }
    onresize = { updateScrollPosition }
/>

{#if !is_near_top}
    <top-actions 
        transition:fade = {{ duration: 150 }}
    >
        <IconButton 
            label = "Back to top" 
            shape = "square" 
            onclick = { scrollToTop }
        >
            <ArrowUp />
        </IconButton>
    </top-actions>
{/if}

<bottom-actions>
    <Button 
        class = "ai-button"
    >
        <Sparkles />
        Ask AI
    </Button>

    {#if !is_near_bottom}
        <down-action 
            transition:slide = {{ duration: 200 }}
        >
            <IconButton 
                label = "Go to bottom" 
                shape = "square" 
                onclick = { scrollToBottom }
            >
                <ArrowDown />
            </IconButton>
        </down-action>
    {/if}
</bottom-actions>



<style>
    top-actions,
    bottom-actions
    {
        position: fixed;
        right: 8rem;
        translate: 50% 0;
        z-index: 20;

        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 12px;

        :global(button.square.square)
        {
            color: var(--text-placeholder);
        }

        :global(.ai-button > content)
        {
            display: flex;
            align-items: center;
            gap: 12px;
            font-weight: 600;
        }
    }

    top-actions
    {
        top: calc(var(--header-height) + 24px);
    }

    bottom-actions
    {
        bottom: 24px;
        gap: 0;
        
        down-action
        {
            display: block;
            padding-top: 12px;
        }
    }

</style>