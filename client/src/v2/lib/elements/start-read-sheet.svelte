<script lang="ts">
    import CheckableMessage from "../components/_other/checkable-message.svelte";
    import DateListItem from "../components/list-items/date-list-item.svelte";
    import { Dates } from "../utils/dates";
    import ArrowLeft from "../components/icons/arrow-left.svelte";
    import EditionListItem from "../components/list-items/edition-list-item.svelte";
    import { FormatColorRegistry } from "../utils/format-color-registry";
    import type { PickableEdition } from "../types/pickable-edition.type";
    import Sheet from "../components/_other/bottom-sheet.svelte";
    import Button       from "@v2/lib/components/buttons/button.svelte";
    import IconButton   from "@v2/lib/components/buttons/icon-button.svelte";



    let {
        is_sheet_opened = $bindable(),
        start_date      = $bindable(),
        end_date        = $bindable(),
        is_dnf          = $bindable(),
        is_historical   = $bindable(),
        editions,
        edition_id      = $bindable(),
        dont_have       = $bindable(),
        onFinish,
    }: {
        is_sheet_opened: boolean
        start_date:      string
        end_date:        string
        is_dnf:          boolean
        is_historical:   boolean
        editions:        PickableEdition[]
        edition_id:      string
        dont_have:       boolean
        onFinish:        () => void
    } = $props()


    let sheet_step: number = $state(1)

    const is_start_date_required: boolean = $derived(!is_historical)
    const is_start_date_filled:   boolean = $derived(start_date.length === 10 && Dates.isISOdateValid(start_date))
    const is_end_date_required:   boolean = $derived(is_dnf && !is_historical)
    const is_end_date_filled:     boolean = $derived(end_date.length === 10 && Dates.isISOdateValid(end_date))
    const can_continue:           boolean = $derived((!is_start_date_required || is_start_date_filled) && (!is_end_date_required || is_end_date_filled))
</script>



<Sheet
    bind:is_open = { is_sheet_opened }
    lock_to_fit  = { true }
>
    {#if sheet_step === 1}
        <list
            class = "dates"
        >
            <DateListItem 
                key           = "Start date"
                bind:ISO_date = { start_date }
                is_disabled   = { is_historical }
                is_required   = { is_start_date_required }
            />
            <DateListItem 
                key           = "End date"
                bind:ISO_date = { end_date }
                is_disabled   = { is_historical }
                is_required   = { is_end_date_required }
            />
        </list>

        <list 
            class = "modifiers"
        >
            <CheckableMessage 
                message = "Did not finish (DNF)"
                bind:is_checked = { is_dnf }
            />

            <CheckableMessage 
                message = "Is historical"
                bind:is_checked = { is_historical }
            />
        </list>
    {:else}
        <h1>Pick the edition you are reading</h1>

        <list>
            {#each editions as edition (edition.id)}
                <EditionListItem 
                    variant     = { FormatColorRegistry.getVariant(edition.variant_id) }
                    is_checked  = { edition_id === edition.id }
                    is_disabled = { dont_have }
                    onChange    = { () => edition_id = edition.id }
                />
            {/each}
        </list>

        <CheckableMessage 
            message = "I don't have this edition"
            bind:is_checked = { dont_have }
        />
    {/if}
        
    <wrapper>
        <IconButton 
            class   = { [ "back", sheet_step > 1 && "open" ] }
            shape   = "round"
            label   = "back"
            onclick = { () => sheet_step = 1 }
        >
            <ArrowLeft />
        </IconButton>

        <Button
            size    = "fat"
            variant = "primary"
            state   = { can_continue ? "default" : "disabled" }
            onclick = { sheet_step === 1 ? () => sheet_step = 2 : onFinish }
            class   = "primary-action"
        >
            { sheet_step === 1 ? "Continue" : "Start read" }
        </Button>
    </wrapper>
</Sheet>



<style>
    .modifiers
    {
        display: flex;
        flex-direction: column;
    }

    h1
    {
        font-size: var(--text-body-lg);
        font-weight: 400;
        text-align: center;
    }

    wrapper
    {
        display: flex;
        flex-direction: row;
    }

    :global(button.back)
    {
        width: 0;
        margin-right: 0;
        opacity: 0;
        overflow: hidden;

        transition: width 200ms cubic-bezier(0.05, 0.7, 0.1, 1);

        &.open
        {
            width: 54px;
            margin-right: 4px;
            opacity: 1;
        }
    }

    :global(.primary-action)
    {
        flex: 1;
        transition: width 200ms cubic-bezier(0.05, 0.7, 0.1, 1);
    }
</style>