<script lang="ts">
    import IconButton from "#lib/components/buttons/icon-button.svelte";
    import ChevronLeft from "#lib/components/icons/chevron-left.svelte";
    import ChevronRight from "#lib/components/icons/chevron-right.svelte";
    import Ex from "#lib/components/icons/ex.svelte";
    import type { HomeContent_GallerySection } from "#lib/content/home-content.ts";



    // minimum horizontal distance (px) for a touch to count as a swipe
    const SWIPE_THRESHOLD = 50



    let {
        content
    }: {
        content: HomeContent_GallerySection
    } = $props()

    
    let carousel:  HTMLElement       | undefined = $state()
    let dialog: HTMLDialogElement | undefined = $state()

    let can_scroll_back:    boolean = $state(false)
    let can_scroll_forward: boolean = $state(false)
    let current_index:      number  = $state(0)

    let swipe_start_x: number | null = null

    const current_image = $derived(content.screenshots[current_index])


    $effect(() =>
    {
        updateScrollButtons()
    })


    function updateScrollButtons()
    {
        if (!carousel)
        {
            return
        }

        can_scroll_back    = carousel.scrollLeft > 0
        can_scroll_forward = carousel.scrollLeft + carousel.clientWidth < carousel.scrollWidth - 1
    }


    function scrollCarousel(direction: number)
    {
        carousel?.scrollBy({ left: direction * carousel.clientWidth * 0.8 })
    }


    function openViewer(index: number)
    {
        current_index = index
        dialog?.showModal()
    }


    function closeViewer()
    {
        dialog?.close()
    }


    function showRelative(offset: number)
    {
        current_index = (current_index + offset + content.screenshots.length) % content.screenshots.length
    }


    function handleKeydown(event: KeyboardEvent)
    {
        if (event.key === "ArrowLeft")
        {
            showRelative(-1)
        }

        if (event.key === "ArrowRight")
        {
            showRelative(1)
        }
    }


    function handleDialogClick(event: MouseEvent)
    {
        // a click on the empty area around the image, not on the image or a button
        if (event.target === dialog)
        {
            closeViewer()
        }
    }


    function handlePointerDown(event: PointerEvent)
    {
        swipe_start_x = event.clientX
    }


    function handlePointerUp(event: PointerEvent)
    {
        if (swipe_start_x === null)
        {
            return
        }

        const distance = event.clientX - swipe_start_x
        swipe_start_x = null

        if (Math.abs(distance) < SWIPE_THRESHOLD)
        {
            return
        }

        showRelative(distance < 0 ? 1 : -1)
    }
</script>



<gallery-section>
    <p class = "title">{ content.title }</p>

    <carousel-wrapper>
        <arrow-slot
            style:visibility = { can_scroll_back ? "visible" : "hidden"}
        >
            <IconButton
                label = "previous"
                shape = "square"
                class = "previous"
                onclick = { () => scrollCarousel(-1) }
            >
                <ChevronLeft />
            </IconButton>
        </arrow-slot>

        <image-carousel
            bind:this = { carousel }
            onscroll = { updateScrollButtons }
        >
            {#each content.screenshots as image, index}
                <button 
                    class = "image-preview"
                    onclick = { () => openViewer(index) }
                >
                    <img
                        src = { image.src }
                        alt = { image.alt }
                    />
                </button>
            {/each}
        </image-carousel>

        <arrow-slot
            style:visibility = { can_scroll_forward ? "visible" : "hidden" }
        >
            <IconButton
                label = "next"
                shape = "square"
                class = "next"
                onclick = { () => scrollCarousel(1) }
            >
                <ChevronRight />
            </IconButton>
        </arrow-slot>
    </carousel-wrapper>
</gallery-section>

<dialog
    bind:this     = { dialog }
    aria-label    = "Screenshot viewer"
    onkeydown     = { handleKeydown }
    onclick       = { handleDialogClick }
    onpointerdown = { handlePointerDown }
    onpointerup   = { handlePointerUp }
>
    <IconButton
        label = "close viewer"
        onclick = { closeViewer }
        class = "close-viewer"
        shape = "square"
    >
        <Ex />
    </IconButton>

    <IconButton
        label = "previous"
        class = "previous"
        onclick = { () => showRelative(-1) }
    >
        <ChevronLeft />
    </IconButton>

    <figure>
        <figcaption>
            { current_image.caption }
        </figcaption>

        <image-frame>
            <img 
                src = { current_image.src } 
                alt = { current_image.alt } 
            />
        </image-frame>

        <p class = "progress">{ current_index + 1 } / { content.screenshots.length }</p>
    </figure>

    <IconButton
        label = "next"
        class = "next"
        onclick = { () => showRelative(1) }
    >
        <ChevronRight />
    </IconButton>
</dialog>



<style>
    gallery-section
    {
        display: flex;
        flex-direction: column;
        align-items: center;

        padding: 64px 64px 72px 64px;
        gap: 48px;

        background-color: var(--bg-brand-secondary-dark);

        @media (width <= 1280px)
        {
            padding: 64px 32px 72px 32px;
        }

        @media (width <= 780px)
        {
            padding: 64px 0 72px 0;
        }

        p.title
        {
            color: var(--text-inverted);
            font-size: var(--text-heading-md);
            font-weight: 600;
        }

        carousel-wrapper
        {
            display: flex;
            flex-direction: row;
            align-items: center;
            align-self: stretch;

            gap: 32px;

            @media (width <= 780px)
            {
                arrow-slot
                {
                    display: none;
                }
            }

            image-carousel
            {
                display: flex;
                flex-direction: row;
                flex: 1;
    
                gap: 16px;
    
                overflow-x: auto;
                scroll-snap-type: x mandatory;
                scroll-behavior: smooth;
                scrollbar-width: none;

                @media (width <= 780px) 
                {
                    padding-left: 16px;
                }

                button.image-preview
                {
                    line-height: 0;

                    img
                    {
                        width: 300px;
                        height: auto;
                        
                        border-radius: 16px;
                        border: 1px solid var(--text-placeholder);
                        box-shadow: 4px 4px 4px 0 rgba(0, 0, 0, 0.2);
    
                        scroll-snap-align: start;

                        @media (width <= 1280px)
                        {
                            width: 250px;
                        }

                        @media (width <= 780px)
                        {
                            width: 200px;
                        }
                    }
                }
    
            }
        }
    }

    dialog
    {
        margin: auto;
        border: none;
        background: transparent;
        color: var(--text-inverted);

        &:not([open])
        {
            display: none;
        }
    
        &::backdrop
        {
            background: color-mix(in srgb, var(--text) 85%, transparent 15%);
            backdrop-filter: blur(8px);
        }

        :global(button.close-viewer)
        {
            position: fixed;
            top: 16px;
            left: 32px;
            color: var(--text-inverted)
        }

        :global(button.previous),
        :global(button.next)
        {
            position: fixed;
            top: 50%;
            translate: 0 -50%;
        }

        :global(button.previous)
        {
            left: 32px;
        }

        :global(button.next)
        {
            right: 32px;
        }

        figure
        {
            height: calc(100dvh - 48px);

            display: flex;
            flex-direction: column;
            align-items: center;

            gap: 24px;
            margin: 0;

            figcaption
            {
                font-size: var(--text-heading-md);
            }

            image-frame
            {
                display: flex;
                align-items: center;
                justify-content: center;
                flex: 1;
                min-height: 0;
                width: calc(100vw - 240px);
                container-type: size;

                img
                {
                    width: auto;
                    height: auto;
                    max-width: 100cqw;
                    max-height: 100cqh;

                    border-radius: 24px;
                    border: 1px solid var(--text-placeholder);
                }
            }

            p.progress
            {
                font-size: var(--text-body-lg);
            }
        }
    }

    :global(button.next.next),
    :global(button.previous.previous)
    {
        color: var(--text-inverted);
    }
</style>