<script lang="ts">
    import CaruselPips from "../components/_other/carusel-pips.svelte";
    import type { CurrentRead } from "../stores/library-store.svelte";

    import CurrentlyReadingCard from "./currently-reading-card.svelte";


    let {
        items = [],
        triggerSheet,
    }: {
        items?: CurrentRead[]
        triggerSheet?: (id: string) => void
    } = $props()


    let scroll_index: number = $state(0)

    let current:  number  = $derived(Math.min(scroll_index, Math.max(items.length - 1, 0)))
    let has_pips: boolean = $derived(items.length > 1)

    function updateCurrent(event: Event & { currentTarget: HTMLElement }): void
    {
        const wrapper = event.currentTarget

        if (wrapper.clientWidth === 0) return

        scroll_index = Math.round(wrapper.scrollLeft / wrapper.clientWidth)
    }
</script>



<currently-reading-carusel
    role       = "region"
    aria-label = "currently reading"
>
    <carusel-wrapper
        onscroll = { updateCurrent }
    >
        {#each items as item (item.read_record_id)}
            <CurrentlyReadingCard 
                title    = { item.title }
                author   = { item.author }
                cover_id = { item.cover_id }
                progress = { item.progress }
                onClick  = { () => triggerSheet?.(item.read_record_id) }
            />
        {/each}
    </carusel-wrapper>

    {#if has_pips}
        <CaruselPips 
            count   = { items.length }
            current = { current }
        />
    {/if}
</currently-reading-carusel>



<style>
    currently-reading-carusel
    {
        display: flex;
        flex-direction: column;
        align-items: stretch;
        overflow: hidden;
        flex-shrink: 0;

        & > :global(carusel-pips)
        {
            align-self: center;
        }
    }

    carusel-wrapper
    {
        display: grid;
        grid-auto-flow: column;
        grid-auto-columns: 100%;

        width: 100%;
        min-width: 0;

        overflow-x: auto;
        overscroll-behavior-x: contain;
        scroll-snap-type: x mandatory;
        scrollbar-width: none;
        -ms-overflow-style: none;

        &::-webkit-scrollbar {
            display: none;
        }


        & > :global(*)
        {
            scroll-snap-align: start;
            scroll-snap-stop: always;
            min-width: 0;
        }
    }
</style>