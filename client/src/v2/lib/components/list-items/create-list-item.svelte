<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
    import CircleQuestionMark from "../icons/circle-question-mark.svelte";



    let {
        label,
        helper,
    }: {
        label: string
        helper?: string
    } = $props()


    const id = $props.id()
    const popover_id = `help-popover-${id}`

    let popover_element: HTMLDivElement


    function block(e: Event)
    {
        e.stopPropagation()
        e.preventDefault()
    }

    function closeFromShield(e: MouseEvent)
    {
        block(e)
        popover_element?.hidePopover()
    }
</script>



<create-list-item>
    <button
        class = "create-button"
    >
        <p>Create <span>{ label }</span></p>
    </button>

    <IconButton 
        popovertarget = { popover_id }
        popovertargetaction = "toggle"
        class = "help-button"
        label = "help"
        shape = "square"
    >
        <CircleQuestionMark 
            width = { 16 }
            height = { 16 }
        />
    </IconButton>

    <div
        id = { popover_id }
        popover = "auto"
        class   = "help-popover"
        bind:this={popover_element}
    >
        <popover-shield 
            onpointerdown = { block }
            onclick       = { closeFromShield }
        ></popover-shield>

        <popover-content>
            The name is converted to "Last, First" name, otherwise some features will not work.
        </popover-content>
    </div>
</create-list-item>



<style>
    create-list-item
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        border-bottom: 1px solid var(--text-placeholder);
        padding-left: 8px;
    }

    .create-button
    {
        height: 48px;
        flex: 1;

        & > p > span
        {
            color: var(--text-primary);
            font-weight: 500;
        }
    }

    popover-shield 
    {
        position: fixed;
        inset: 0;
        z-index: -1;
        background: transparent;
    }


    popover-content 
    {
        display: block;
    }

    .help-popover
    {
        margin: 0;
        padding: 16px;
        border: none;
        border-radius: 8px;
        box-shadow: 4px 4px 4px rgba(0, 0, 0, 0.2);
        background: var(--bg-overlay);
        max-width: 220px;
        font-size: var(--text-body-sm);

        position-anchor: auto;
        inset: auto anchor(right) anchor(top) auto;
        margin-bottom: -8px;
        margin-right: 16px;
    }
</style>