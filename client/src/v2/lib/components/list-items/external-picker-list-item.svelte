<script lang="ts">
    import type { HTMLButtonAttributes } from "svelte/elements";
    import ChevronRight from "../icons/chevron-right.svelte";
    import { ripple_effect } from "../buttons/ripple-effect";
    import "../buttons/ripple-effect.css"


    let {
        key,
        label,
        disabled_message,
        is_required = false,
        onclick,
        ...rest
    }: HTMLButtonAttributes & {
        key:          string
        label:        string
        disabled_message?: string
        is_required?: boolean
    } = $props()
</script>



<!-- ------------------------------------ -->



<button
    onclick = { onclick }
    disabled = { disabled_message !== "" && disabled_message !== undefined }
    class = "ripple-container"
    { ...rest }
    {@attach ripple_effect}
>
    <p class = "key">
        { key }

        {#if is_required}
            <span class = "required">*</span>
        {/if}
    </p>

    {#if !disabled_message}
        <value-wrapper>
            <p>{ label }</p>
            <ChevronRight />
        </value-wrapper>
    {:else}
        <p class = "disabled_message">{ disabled_message }</p>
    {/if}
</button>



<!-- ------------------------------------ -->



<style>
    button
    {
        display: flex;
        flex-direction: row;

        padding: 12px 0 12px 8px;

        border-bottom: 1px solid var(--text-placeholder);

        &:disabled
        {
            cursor: auto;
        }
    }

    span.required
    {
        color: var(--text-error)
    }

    value-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 8px;
        align-items: center;
        justify-content: end;
        flex: 1;
    }

    p.disabled_message
    {
        flex: 1;
        text-align: end;
        padding-right: 8px;
        color: var(--text-placeholder);
    }
</style>