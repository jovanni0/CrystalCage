<script lang="ts">
    import Button from "@v2/lib/components/buttons/button.svelte";



    let {
        is_open       = $bindable(false),
        title,
        message,
        confirm_label = "Delete",
        cancel_label  = "Cancel",
        is_busy       = false,
        onConfirm,
    }: {
        is_open?:       boolean
        title:          string
        message:        string
        confirm_label?: string
        cancel_label?:  string
        is_busy?:       boolean
        onConfirm:      () => void | Promise<void>
    } = $props()

    let dialog: HTMLDialogElement


    $effect(() =>
    {
        if (is_open && !dialog.open)
        {
            dialog.showModal()
        }

        if (!is_open && dialog.open)
        {
            dialog.close()
        }
    })



    /* Escape, Back or a backdrop tap close the dialog natively; keep `is_open` in sync */
    function onClose(): void
    {
        is_open = false
    }
</script>



<dialog
    bind:this = { dialog }
    closedby  = "any"
    onclose   = { onClose }
>
    <h2>{ title }</h2>
    <p>{ message }</p>

    <actions-wrapper>
        <Button
            variant = "ghost"
            size    = "slim"
            state   = { is_busy ? "disabled" : "default" }
            onclick = { () => is_open = false }
        >
            { cancel_label }
        </Button>

        <Button
            class   = "destructive"
            size    = "slim"
            state   = { is_busy ? "loading" : "default" }
            onclick = { onConfirm }
        >
            { confirm_label }
        </Button>
    </actions-wrapper>
</dialog>



<style>
    dialog
    {
        width: min(100% - 48px, 360px);
        padding: 24px;
        border: none;
        border-radius: 16px;
        background-color: var(--base);
        color: var(--text);

        &::backdrop
        {
            background-color: rgba(0, 0, 0, 0.4);
        }
    }

    h2
    {
        margin: 0 0 8px 0;
        font-size: 1.5rem;
    }

    p
    {
        margin: 0;
        color: var(--text-secondary);
        font-size: 1rem;
        line-height: 1.4;
    }

    actions-wrapper
    {
        display: flex;
        justify-content: flex-end;
        gap: 8px;
        margin-top: 24px;
    }

    actions-wrapper > :global(.button-mimic.primary.destructive)
    {
        background-color: var(--red-primary);
    }
</style>