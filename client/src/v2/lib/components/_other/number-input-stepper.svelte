<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
    import ChevronsUpDown from "../icons/chevrons-up-down.svelte";
    import Minus from "../icons/minus.svelte";
    import Plus from "../icons/plus.svelte";



    let {
        value     = $bindable(),
        min_value = 1,
        max_value = 999,
    }: {
        value:      number
        min_value?: number
        max_value?: number
    } = $props()
</script>



<number-input-stepper>
    <IconButton
        label = "decrement"
        shape = "square"
        onclick = { () => value > min_value ? value = value - 1 : undefined }
    >
        <Minus />
    </IconButton>

    <input-wrapper>
        <input
            type = "number"
            bind:value
        >

        <ChevronsUpDown />
    </input-wrapper>

    <IconButton
        label = "increment"
        shape = "square"
        onclick = { () => value < max_value ? value = value + 1 : undefined }
    >
        <Plus />
    </IconButton>
</number-input-stepper>



<style>
    number-input-stepper
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;
        align-self: center;
    }

    input
    {
        all: unset;
        -webkit-appearance: none;
        appearance: none;

        min-height: 48px;
        min-width: 0;
        width: 96px;
        flex: 1;

        font-family: Manrope;
        font-size: var(--text);
        background-color: rgba(0, 0, 0, 0.1);
        padding: 6px 36px 6px 12px;
        border-radius: 8px;
        text-align: end;
        box-sizing: border-box;

        &:focus
        {
            outline: 2px solid var(--text-brand);
        }
    }

    input[type="number"] 
    {
        -moz-appearance: textfield;

        &::-webkit-inner-spin-button,
        &::-webkit-outer-spin-button
        {
            -webkit-appearance: none;
            margin: 0;
        }
    }

    input-wrapper 
    {
        position: relative;

        & > :global(svg)
        {
            position: absolute;
            top: 50%;
            right: 8px;
            transform: translateY(-50%);
        }
    }
</style>