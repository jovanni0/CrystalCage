<script lang="ts">
    import type { EditionWordCount } from "../stores/book-management.store.svelte";
    import      { edition_format_color_registry } from "@root/CompositionRoot";
    import      SliderStepper                     from "../components/_other/slider-stepper.svelte";
    import      Button                            from "../components/buttons/button.svelte";
    import      InlineHelpButton                  from "../components/buttons/inline-help-button.svelte";
    import      EditionWordcountListItem          from "../components/list-items/edition-wordcount-list-item.svelte";
    import      BottomSheet                       from "../components/_other/bottom-sheet.svelte";



    let {
        is_open         = $bindable(),
        value           = $bindable(),
        slider_min,
        slider_max,
        wpp_coeficient,
        editions,
    }: {
        is_open:        boolean
        value:          number | null
        slider_min:     number
        slider_max:     number
        wpp_coeficient: number
        editions:       EditionWordCount[]
    } = $props()

    let draft_count = $derived.by( () =>
    {
        is_open

        return value ? value : 0
    })
</script>



<BottomSheet
    bind:is_open
    policy = "fit-expand"
>
    <SliderStepper 
        bind:value = { draft_count }
        label_prefix = "~ "
        label_suffix = " K"
        min_value  = { slider_min }
        max_value  = { slider_max }
    />

    <message-wrapper
        class:with-help = { draft_count > 0 }
    >
        {#if draft_count === 0}
            <p>Drag the slider to set the book's length.</p>
        {:else}
            <p>{ draft_count * 1000 } words are about { Math.round(draft_count / wpp_coeficient) } pages.</p>

            <InlineHelpButton 
                message = "Pages are estimated using your words-per-page setting (currently { wpp_coeficient * 100 }). The slider's range is also adjustable."
            />
        {/if}
    </message-wrapper>

    {#if editions.length > 0}
        <editions-list>
            {#each editions as edition (edition.id)}
                <EditionWordcountListItem 
                    label                   = { edition.label }
                    added_at                = { edition.added_at }
                    word_count              = { undefined }
                    format_color_definition = { edition_format_color_registry.resolve(edition.format) }
                />
            {/each}
        </editions-list>
    {/if}

    {#snippet footer()}
        <Button
            state   = { draft_count === (value ?? 0) ? "disabled" : "default" }
            onclick = { () => { draft_count > 0 ? value = draft_count : value = null; is_open = false } }
        >
            { draft_count > 0 ? "Set this length" : "Remove length" }
        </Button>
    {/snippet}
</BottomSheet>



<style>
    message-wrapper
    {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;
        min-height: 54px;

        &.with-help
        {
            padding-left: 38px;
        }
    }
</style>