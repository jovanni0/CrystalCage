<script lang="ts">
    import { Dates } from "../../utils/dates";
    import Calendar  from "../icons/calendar.svelte";


    
    let {
        key,
        ISO_date    = $bindable(),
        is_disabled = false,
        is_required = false,
    }: {
        key:          string
        ISO_date:     string
        is_disabled?: boolean
        is_required?: boolean
    } = $props()


    
    let native_input: HTMLInputElement

    let RISO_date: string  = $state(Dates.ISOtoRISO(ISO_date))
    let is_valid:  boolean = $state(true)


    function handleNativeChange(): void
    {
        ISO_date = native_input.value
        RISO_date = Dates.ISOtoRISO(ISO_date)
        is_valid = true
    }


    function handleTypedChange(e: Event): void
    {
        const target = e.currentTarget as HTMLInputElement

        RISO_date = Dates.RISOinsertDashes(target.value)
        ISO_date = Dates.RISOtoISO(RISO_date)
        target.value = RISO_date
        
        if (RISO_date.length === 10)
        {
            is_valid = Dates.isRISOdateValid(RISO_date)

            if (is_valid)
            {
                native_input.value = Dates.RISOtoISO(RISO_date)
            }
        }
        else
        {
            is_valid = true
        }
    }
</script>



<date-list-item
    class:disabled = { is_disabled }
>
    <key-wrapper>
        <label for = "date-input">
            { key }
        </label>

        {#if is_required}
            <span class = "required">*</span>
        {/if}
    </key-wrapper>

    <input-wrapper>
        <input 
            id            = "date-input"
            type          = "text" 
            class         = "styled-date"
            placeholder   = "dd-mm-yyyy"
            inputmode     = "numeric"
            value         = { RISO_date }
            oninput       = { handleTypedChange }
            title         = { is_valid ? "" : "Date is invalid." }
            class:invalid = { !is_valid }
            disabled      = { is_disabled }
        >
    
        <input
            type      = "date"
            class     = "native-date"
            tabindex  = "-1"
            bind:this = { native_input }
            onchange  = { handleNativeChange }
            disabled  = { is_disabled }

        >
    </input-wrapper>

    <button 
        class    = "calendar-trigger"
        onclick  = { () => native_input.showPicker() }
        disabled = { is_disabled }
    >
        <Calendar />
    </button>
</date-list-item>



<style>
    date-list-item
    {
        display: flex;
        flex-direction: row;
        align-items: center;

        border-bottom: 1px solid var(--text-placeholder);
        padding: 0 0 0 8px;
    }

    date-list-item.disabled
    {
        color: var(--text-placeholder);

        .required
        {
            color: var(--text-error-muted);
        }

        .calendar-trigger
        {
            cursor: default;
        }

        .styled-date
        {
            &, &.invalid
            {
                outline: none;
            }

            &, &::placeholder
            {
                color: transparent;
            }
        }
    }

    key-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 2px;
        flex: 1;

        .required
        {
            color: var(--text-error);
        }
    }


    .calendar-trigger
    {
        width: 48px;
        height: 48px;
        border-radius: 8px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    
    input-wrapper
    {
        position: relative;
        
        .styled-date
        {
            border: none;
    
            width: 128px;
            box-sizing: border-box;
    
            padding: 8px 16px;
            background-color: rgba(0, 0, 0, 5%);
            font-family: Manrope;
            font-size: 1rem;
            line-height: 1rem;
            border-radius: 8px;
            text-align: right;

            outline-offset: -2px;

            &.invalid
            {
                outline: 2px solid var(--text-error);
            }

            &:focus
            {
                outline: 2px solid var(--text-brand-secondary-muted);
            }
        }

        .native-date
        {
            position: absolute;
            inset: 0;
    
            z-index: -1;
            opacity: 0;
            pointer-events: none;
        }
    }
</style>