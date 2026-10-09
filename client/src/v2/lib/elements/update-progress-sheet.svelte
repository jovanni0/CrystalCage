<script lang="ts">
    import type { CurrentRead }                    from "../stores/library-store.svelte"
    import      { edition_format_color_registry,
                  read_page_count_storage,
                  nav }                            from "@root/CompositionRoot"
    import      BottomSheet                        from "../components/_other/bottom-sheet.svelte"
    import      CoverImage                         from "../components/covers/cover-image.svelte"
    import      FormatPill                         from "../components/pills/format-pill.svelte"
    import      SliderStepper                      from "../components/_other/slider-stepper.svelte"
    import      ChevronsUpDown                     from "../components/icons/chevrons-up-down.svelte"
    import      Button                             from "../components/buttons/button.svelte"
    import      IconButton                         from "../components/buttons/icon-button.svelte"
    import      ExternalLink                       from "../components/icons/external-link.svelte"


    let {
        is_open = $bindable(),
        read,
        is_saving,
        error,
        onSave,
    }: {
        is_open:   boolean
        read:      CurrentRead | null
        is_saving: boolean
        error:     string | null
        onSave:    (progress: number) => Promise<void>
    } = $props()


    /* names used by the markup */
    const cover_id      = $derived(read?.cover_id ?? null)
    const title         = $derived(read?.title ?? "")
    const edition_label = $derived(read?.edition_label ?? null)
    const book_id       = $derived(read?.book_id ?? "")

    const format_color_definition = $derived(edition_format_color_registry.resolve(read?.format ?? "unknown"))


    /* the percentage is the server value; the page count is display-only and lives on the device */
    const saved_percentage = $derived(Math.round(read?.progress ?? 0))
    const read_id          = $derived(read?.read_record_id ?? null)

    let draft_percentage = $derived.by(() =>
    {
        is_open

        return saved_percentage
    })

    let saved_page_count: number | null             = $state(null)
    let draft_page_count: number | null | undefined = $state(null)


    const page_count: number | null = $derived(isValidPageCount(draft_page_count) ? draft_page_count : null)

    const current_page: number | undefined = $derived.by(() =>
    {
        if (page_count === null)
        {
            return undefined
        }

        return Math.round(page_count * (draft_percentage / 100))
    })

    const has_progress_changed   = $derived(draft_percentage !== saved_percentage)
    const has_page_count_changed = $derived(page_count !== saved_page_count)

    /* also false while saving, so the button disables itself and cannot be tapped twice */
    const has_changed = $derived(!is_saving && (has_progress_changed || has_page_count_changed))


    /* reload the stored page count when the sheet opens, but only react to a different read, not to every rebuild of `read` */
    $effect(() =>
    {
        if (!is_open || read_id === null)
        {
            return
        }

        const current_read_id = read_id
        let is_cancelled      = false

        read_page_count_storage.load(current_read_id).then(stored =>
        {
            if (is_cancelled)
            {
                return
            }

            saved_page_count = stored
            draft_page_count = stored
        })

        return () =>
        {
            is_cancelled = true
        }
    })



    function isValidPageCount(value: number | null | undefined): value is number
    {
        return typeof value === "number" && Number.isInteger(value) && value > 0
    }


    async function onUpdateProgress(): Promise<void>
    {
        if (read === null)
        {
            return
        }

        if (has_page_count_changed)
        {
            await read_page_count_storage.save(read.read_record_id, page_count)
            saved_page_count = page_count
        }

        if (!has_progress_changed)
        {
            is_open = false
            return
        }

        // the store closes the sheet on success and sets `error` on failure
        await onSave(draft_percentage)
    }
</script>




<BottomSheet
    bind:is_open = { is_open }
>
    {#snippet header()}
        <edition-wrapper>
            <cover-image-wrapper>
                <CoverImage 
                    cover_id   = { cover_id }
                    cover_type = "thumbnail"
                    alt        = { title }
                />
            </cover-image-wrapper>

            <info-wrapper>
                <p class = "title">{ title }</p>

                <caption-wrapper>
                    <FormatPill 
                        label  = { format_color_definition.label }
                        hue    = { format_color_definition.hue }
                        chroma = { format_color_definition.chroma }
                    />
                    
                    {#if edition_label}
                        <p class = "edition-label">{ edition_label }</p>
                    {/if}
                </caption-wrapper>
            </info-wrapper>
        </edition-wrapper>
    {/snippet}

    <progress-wrapper>
        <SliderStepper 
            bind:value   = { draft_percentage }
            label_suffix = "%"
            min_value    = { 0 }
            max_value    = { 100 }
        />

        <progress-caption-wrapper>
            <p class = "progress-caption">Currently at {current_page !== undefined ? current_page : "---"} out of</p>
            
            <input-wrapper>
                <input
                    type = "number"
                    bind:value={draft_page_count}
                >

                <ChevronsUpDown />
            </input-wrapper>

            <p class = "progress-caption">reader pages.</p>
        </progress-caption-wrapper>
    </progress-wrapper>


    {#snippet footer()}
        <buttons-wrapper>
            <Button
                class = "PAB"
                state = { has_changed ? "default" : "disabled" }
                onclick = { onUpdateProgress }
            >
                Save progress
            </Button>

            <IconButton
                label = "open book page"
                onclick = { () => nav.goTo(`/book/${book_id}`) }
            >
                <ExternalLink />
            </IconButton>
        </buttons-wrapper>
    {/snippet}
</BottomSheet>



<style>
    edition-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 16px;
        padding: 0 16px 16px 16px;
        align-items: center;
        
        cover-image-wrapper
        {
            width: 50px;
            aspect-ratio: 2/3;
        }

        info-wrapper
        {
            display: flex;
            flex-direction: column;
            gap: 8px;

            caption-wrapper
            {
                display: flex;
                flex-direction: row;
                align-items: center;
                gap: 4px;
                
                p.edition-label
                {
                    font-size: var(--text-body-sm);
                    color: var(--text-muted);
                }
            }
        }
    }
    

    progress-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 0 0 32px 0;

        & > :global(stepper-slider)
        {
            align-self: stretch;
        }

        progress-caption-wrapper
        {
            display: flex;
            flex-direction: row;
            gap: 4px;
            align-items: center;

            p.progress-caption
            {
                color: var(--text-muted);
                font-variant-ligatures: none;
            }

            input-wrapper 
            {
                position: relative;

                & > :global(svg)
                {
                    position: absolute;
                    top: 50%;
                    right: 8px;
                    transform: translateY(-50%);
                }

                input
                {
                    all: unset;
                    -webkit-appearance: none;
                    appearance: none;
    
                    min-height: 36px;
                    min-width: 0;
                    width: 80px;
                    flex: 1;
    
                    font-family: Manrope;
                    font-size: var(--text-body);
                    background-color: rgba(0, 0, 0, 0.1);
                    padding: 6px 36px 6px 12px;
                    border-radius: 8px;
                    text-align: end;
                    box-sizing: border-box;
    
                    &:focus
                    {
                        outline: 2px solid var(--text-brand);
                    }
                }
    
                input[type="number"] 
                {
                    -moz-appearance: textfield;
    
                    &::-webkit-inner-spin-button,
                    &::-webkit-outer-spin-button
                    {
                        -webkit-appearance: none;
                        margin: 0;
                    }
                }
            }
        }
    }

    buttons-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 4px;

        & > :global(.PAB)
        {
            flex: 1;
        }
    }
</style>