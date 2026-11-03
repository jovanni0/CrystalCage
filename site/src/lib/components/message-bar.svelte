<script lang="ts">
    import IconButton from "./buttons/icon-button.svelte";
    import SendHorizontal from "./icons/send-horizontal.svelte";


    let {
        value    = $bindable(),
        disabled = false,
        placeholder,
        onSubmit,
    }: {
        value:       string
        disabled?:   boolean
        placeholder: string
        onSubmit:    (text: string) => void
    } = $props()


    function handleSubmit(event: SubmitEvent)
    {
        event.preventDefault()

        if (disabled || value.trim() === "")
        {
            return
        }

        onSubmit(value)
        value = ""
    }
</script>



<form class = "message-bar" onsubmit = { handleSubmit }>
        <input 
        type = "text"
        placeholder = { placeholder }
        bind:value
    >

    <IconButton 
        label = "send" 
        shape = "square"
        class = "send"
        type  = "submit"
        state = { disabled || !value.trim() ? "disabled" : "default" }
    >
        <SendHorizontal />
    </IconButton>
</form>



<style>
    form.message-bar
    {
        display: flex;
        flex-direction: row;

        border-radius: 16px;
        border: 1px solid var(--border-neutral-default);

        padding-left: 16px;

        overflow: hidden;

        background-color: var(--bg-overlay-subtle);


        &:has(input:focus-visible)
        {
            border: 2px solid var(--text-brand-default);
            margin: -1px;
        }
    }


    input
    {
        all: unset;
        -webkit-appearance: none;
        appearance: none;

        height: 48px;
        min-width: 0;
        flex: 1;

        font-family: Manrope;
        font-size: var(--text);
        background-color: transparent;

        &:focus
        {
            outline: none;
        }

        &::-webkit-search-cancel-button 
        {
            -webkit-appearance: none;
            appearance: none;
        }
    }


    :global(button.send.disabled)
    {
        color: var(--text-neutral-faint) !important;
    }
</style>