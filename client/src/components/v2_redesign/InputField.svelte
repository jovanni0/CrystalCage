<script lang="ts">
    import type { HTMLInputAttributes } from "svelte/elements";
    import Spinner from "./Spinner.svelte";

    let {
        label,
        value = $bindable(""),
        error,
        preallocate = false,
        loading = false,
        style,
        ...rest
    }: HTMLInputAttributes & {
        label:        string
        value?:       string
        error?:       string
        preallocate?: boolean
        loading?:     boolean
        style?:       string
    } = $props()
</script>



<div 
    class = "input-filed"
    style = { style }
>
    <fieldset>
        <legend>{ label }</legend>
        <input
            bind:value
            { ...rest }
        >

        {#if loading}
            <div class = "spinner-wrapper">
                <Spinner class = "input-spinner" />
            </div>
        {/if}
    </fieldset>
    
    {#if preallocate || error}
        <p class = "error-message">{ error }</p>
    {/if}
</div>



<style>
    .input-filed
    {
        display: flex;
        flex-direction: column;
        gap: 2px;
        align-self: stretch;
    }

    fieldset
    {
        all: unset;

        position: relative;
        display: block;
        border: 1px solid var(--text);
        padding: 4px 16px 12px 16px;
        border-radius: 16px;
        align-self: stretch;
    }

    legend
    {
        font-size: var(--text-body-sm);
        color: var(--text);
    }

    input
    {
        all: unset;
        width: 100%;
        padding-right: 24px;
        box-sizing: border-box;
        font-size: var(--text-body);
    }

    .error-message
    {
        font-size: var(--text-body-sm);
        color: var(--text-error);
        padding-left: 16px;
    }

    p
    {
        min-height: 1.2rem;
    }

    .spinner-wrapper
    {
        position: absolute;
        top: 50%;
        right: 16px;
        transform: translateY(calc(-50% - 2px));

        :global(.input-spinner)
        {
            color: var(--text-muted);
        }
    }
</style>