<script lang="ts">
    let {
        is_completed
    }: {
        is_completed: boolean
    } = $props()
</script>



<timeline-marker class:completed = { is_completed }>
    <dot-circle class:completed = { is_completed }></dot-circle>
</timeline-marker>



<style>
    timeline-marker
    {
        --dot-size: 24px;
        --line-width: 2px;
        --dash: 12px;
        --dash-gap: 8px;

        display: grid;
        grid-template-rows: 1fr var(--dot-size) 1fr;
        justify-items: center;

        color: var(--text-neutral-subtle);


        &::before,
        &::after
        {
            content: "";

            width: var(--line-width);
            height: 100%;
        }


        &::before
        {
            grid-row: 1;
            background: repeating-linear-gradient(
                to top,
                currentColor 0 var(--dash),
                transparent var(--dash) calc(var(--dash) + var(--dash-gap))
            );
        }


        &::after
        {
            grid-row: 3;
            background: repeating-linear-gradient(
                to bottom,
                currentColor 0 var(--dash),
                transparent var(--dash) calc(var(--dash) + var(--dash-gap))
            );
        }


        &.completed
        {
            color: var(--bg-brand-strong);
        }
    }


    dot-circle
    {
        grid-row: 2;

        width: var(--dot-size);
        height: var(--dot-size);
        box-sizing: border-box;

        border: var(--line-width) solid currentColor;
        border-radius: 50%;
        background-color: transparent;


        &.completed
        {
            color: var(--text-brand-default);
            background-color: var(--bg-brand-subtle);
        }
    }
</style>