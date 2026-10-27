<script lang="ts">
    import ChevronDown from "./icons/chevron-down.svelte";



    type SelectOption = {
        value: string
        label: string
    }


    let {
        value        = $bindable(),
        preallocate  = false,
        is_mandatory = false,
        label,
        options,
        error,
    }: {
        value?:        string
        preallocate?:  boolean
        is_mandatory?: boolean
        label:         string
        options:       SelectOption[]
        error?:        string
    } = $props()


    const id = $props.id()
</script>



<input-select>
    <fieldset>
        <legend id = "{ id }-legend">
            { label }

            {#if is_mandatory}
                <span class = "mandatory-mark">*</span>
            {/if}
        </legend>

        <select
            bind:value
            aria-labelledby  = "{ id }-label"
            aria-invalid     = { error ? "true" : undefined }
            aria-describedby = { error ? `${ id }-error` : undefined }
        >
            <button>
                <selectedcontent></selectedcontent>
                <ChevronDown class = "chevron" />
            </button>
        
            {#each options as option (option.value)}
                <option value = { option.value }>{ option.label }</option>
            {/each}
        </select>
    </fieldset>

    {#if preallocate || error}
        <p class = "error-message">{ error }</p>
    {/if}
</input-select>



<style>
    input-select
    {
        display: flex;
        flex-direction: column;
        align-self: stretch;

        gap: 2px;
    }


    fieldset
    {
        all: unset;

        height: 56px;
        box-sizing: border-box;
        overflow: hidden;

        position: relative;
        display: block;
        align-self: stretch;

        border: 1px solid var(--text-neutral-default);
        border-radius: 16px;

        margin: 0 1px 1px 1px; /* to offset the border thickness shift when in focus */


        &:has(select:focus-visible),
        &:has(select:open)
        {
            border: 2px solid var(--text-brand-default);
            margin: 0px;
        }

        &:has(select:open)
        {
            :global(svg.chevron)
            {
                transform: rotate(180deg);
            }
        }
    }


    legend
    {
        font-size: var(--text-body-sm);
        color: var(--text-neutral-default);

        margin-left: 16px;
    }


    p.error-message
    {
        font-size: var(--text-body-sm);
        color: var(--text-danger-default);

        padding-left: 16px;
        min-height: 1.2rem;
    }


    span.mandatory-mark
    {
        color: var(--text-danger-default);
    }


    select,
    ::picker(select)
    {
        appearance: base-select;
    }


    select
    {
        width: 100%;

        padding: 4px 16px;

        border: none;
        border-radius: 0;
        background: none;
        outline: none;

        font: inherit;
        font-size: var(--text-body);
        color: inherit;
        cursor: pointer;


        &::picker-icon
        {
            display: none;
        }
    }


    :global(svg.chevron)
    {
        transition: transform 0.5s;
    }


    selectedcontent
    {
        flex: 1;
    }


    ::picker(select)
    {
        background-color: var(--bg-surface-raised);

        border: 1px solid var(--border-neutral-default);
        border-radius: 16px;

        box-shadow: 4px 4px 4px 0 rgba(0, 0, 0, 0.2);

        margin-top: 8px;
    }


    option
    {
        min-height: 48px;
        padding: 0 16px;


        &::checkmark
        {
            display: none;
        }
    }
</style>