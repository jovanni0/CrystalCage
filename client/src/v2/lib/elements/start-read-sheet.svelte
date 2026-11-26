<script lang="ts">
    import CheckableMessage from "../components/_other/checkable-message.svelte";
    import DateListItem from "../components/list-items/date-list-item.svelte";
    import { DateRules } from "../rules/date-rules";
    import EditionListItem from "../components/list-items/edition-list-item.svelte";
    import type { PickableEdition } from "../types/pickable-edition.type";
    import BottomSheet from "../components/_other/bottom-sheet.svelte";
    import Button       from "@v2/lib/components/buttons/button.svelte";
    import IconButton   from "@v2/lib/components/buttons/icon-button.svelte";
    import { edition_format_color_registry } from "@root/CompositionRoot";
    import ArrowLeft from "../components/icons/arrow-left.svelte";



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
        edition_id:      string | null
        dont_have:       boolean
        onFinish:        () => void
    } = $props()


    let sheet_step: number = $state(1)

    const is_start_date_required: boolean = $derived(!is_historical)
    const is_start_date_filled:   boolean = $derived(start_date.length === 10 && DateRules.isISOdateValid(start_date))
    const is_end_date_required:   boolean = $derived(is_dnf && !is_historical)
    const is_end_date_filled:     boolean = $derived(end_date.length === 10 && DateRules.isISOdateValid(end_date))
    const can_continue:           boolean = $derived((!is_start_date_required || is_start_date_filled) && (!is_end_date_required || is_end_date_filled))


    $effect(() =>
    {
        if (is_sheet_opened === false)
        {
            sheet_step = 1
        }
    })

    console.log(edition_id)
</script>



<BottomSheet
    bind:is_open = { is_sheet_opened }
    policy = "fit"
>
    {#if sheet_step === 1}
        <step-1-wrapper>
            <date-list>
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
            </date-list>

            <modifier-list>
                <CheckableMessage 
                    message = "Did not finish (DNF)"
                    bind:is_checked = { is_dnf }
                />

                <CheckableMessage 
                    message = "Is historical"
                    bind:is_checked = { is_historical }
                />
            </modifier-list>
        </step-1-wrapper>
    {:else}
        <step-2-wrapper>
            <h1>Pick the edition you are reading</h1>

            <edition-list>
                {#each editions as edition (edition.id)}
                    <EditionListItem 
                        label       = { edition.label }
                        added_at    = { edition.added_at }
                        variant     = { edition_format_color_registry.resolve(edition.format_id) }
                        is_checked  = { edition_id === edition.id }
                        is_disabled = { dont_have }
                        onChange    = { () => edition_id = edition.id }
                    />
                {/each}
            </edition-list>

            <CheckableMessage 
                message = "I don't have this edition"
                bind:is_checked = { dont_have }
            />
        </step-2-wrapper>
    {/if}

    {#snippet footer()}
        <button-wrapper>
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
        </button-wrapper>
    {/snippet}
</BottomSheet>



<style>
    step-1-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 24px;
        
        modifier-list
        {
            display: flex;
            flex-direction: column;
        }
    }

    step-2-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 24px;

        h1
        {
            font-size: var(--text-body-lg);
            font-weight: 400;
            text-align: center;
            margin-bottom: -16px;
        }
    }

    button-wrapper
    {
        display: flex;
        flex-direction: row;
        margin-top: 16px;

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
    
        :global(button.primary-action)
        {
            flex: 1;
            transition: width 200ms cubic-bezier(0.05, 0.7, 0.1, 1);
        }
    }

</style>