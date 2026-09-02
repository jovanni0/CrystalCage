<script lang="ts">
    import type { HTMLAttributes } from "svelte/elements";
    import Spinner from "./Spinner.svelte";



    let { 
        children,
        variant = "PRIMARY",
        state = "DEFAULT",
        href,
        onclick,
        class: className,
        ...rest
    }: HTMLAttributes<HTMLButtonElement> & HTMLAttributes<HTMLAnchorElement> & {
        children: any
        variant?: "PRIMARY" | "GHOST-NEUTRAL" | "GHOST-ACCENT"
        state?:   "DEFAULT" | "LOADING" | "DISABLED"
        href?:    string
        onclick?: () => void
        className?: string
    }= $props()


    const tag = $derived(href ? "a" : "button")
</script>



<svelte:element
    this          = { tag }
    href          = { href }
    role          = { href ? "button" : undefined }
    aria-disabled = { state === "DISABLED" || state === "LOADING" }
    tabindex      = { state === "DISABLED" ? -1 : undefined }
    onclick       = { state === "DEFAULT" ? onclick : undefined }
    class         = { `${variant.toLowerCase()} ${state.toLowerCase()} ${className}` }
    { ...rest }
>
    <div class:content-loading = { state === "LOADING" }>
        {@render children()}
    </div>

    {#if state === "LOADING"}
        <div class = "spinner-wrapper">
            <Spinner />
        </div>
    {/if}
</svelte:element>



<style>
    button, a
    {
        border: none;
        text-decoration: none;
        background: none;

        position: relative;
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        padding: 16px 32px;
        border-radius: 999px;
    }

    .content-loading
    {
        visibility: hidden;
    }

    .spinner-wrapper
    {
        position: absolute;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .primary
    {
        background-color: var(--bg-brand);
        color: var(--text-inverted);

        &.disabled
        {
            background-color: var(--bg-brand-disabled);
        }
    }

    .ghost-neutral
    {
        background-color: transparent;
        color: var(--text);
    }

    .ghost-accent
    {
        background-color: transparent;
        color: var(--text-brand-primary);
    }
</style>