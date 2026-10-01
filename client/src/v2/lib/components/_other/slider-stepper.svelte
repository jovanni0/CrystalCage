<script lang="ts">
    import IconButton from "../buttons/icon-button.svelte";
    import Minus from "../icons/minus.svelte";
    import Plus from "../icons/plus.svelte";



    let {
        value = $bindable(),
        min_value = 0,
        max_value = 300,
    }: {
        value: number,
        min_value?: number
        max_value?: number
    } = $props()


    const progress = $derived( (value - min_value) / (max_value - min_value) );
    const label_value = $derived(Math.floor(min_value + progress * (max_value - min_value)))
</script>



<stepper-slider>
    <IconButton
        label = "decrement"
        shape = "square"
    >
        <Minus />
    </IconButton>
    
    <track-wrapper
        style:--progress = { progress }
    >
        <input
            type       = "range"
            min        = { min_value }
            max        = { max_value }
            bind:value
        >

        <span>~ {label_value} K</span>
    </track-wrapper>

    <IconButton
        label = "increment"
        shape = "square"
    >
        <Plus />
    </IconButton>
</stepper-slider>



<style>
    stepper-slider
    {
        display: flex;
        align-items: center;
        gap: 8px;
    }

    track-wrapper
    {
        --thumb: 20px;
        --fill: calc(var(--thumb) / 2 + (100% - var(--thumb)) * var(--progress, 0));

        position: relative;
        display: block;
        flex: 1;
        min-width: 0;
        padding: 1.75rem 0;

        & > span
        {
            position: absolute;
            top: 0;
            left: clamp(1.5em, var(--fill), calc(100% - 1.5em));
            transform: translateX(-50%);
            white-space: nowrap;
            pointer-events: none;
        }
    }

    input[type="range"]
    {
        width: 100%;
        min-width: 0;
        height: var(--thumb);
        margin: 0;
        -webkit-appearance: none;
        appearance: none;
        background: transparent;
    }

    input[type="range"]::-webkit-slider-runnable-track
    {
        height: 8px;
        border-radius: 4px;
        background: linear-gradient(
            to right,
            var(--bg-brand) var(--fill),
            var(--text-inverted-muted) var(--fill)
        );
    }

    input[type="range"]::-webkit-slider-thumb
    {
        -webkit-appearance: none;
        width: var(--thumb);
        height: var(--thumb);
        margin-top: -6px;
        border-radius: 50%;
        background-color: var(--bg-brand);
    }

    input[type="range"]::-moz-range-track
    {
        height: 8px;
        border-radius: 4px;
        background-color: var(--text-inverted-muted);
    }

    input[type="range"]::-moz-range-progress
    {
        height: 8px;
        border-radius: 4px;
        background-color: var(--bg-brand);
    }

    input[type="range"]::-moz-range-thumb
    {
        width: var(--thumb);
        height: var(--thumb);
        border: none;
        border-radius: 50%;
        background-color: var(--bg-brand);
    }
</style>