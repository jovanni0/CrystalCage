<script lang="ts">
    import type { VolumeLabel }    from "../stores/book-management.store.svelte";
    import      NumberInputStepper from "../components/_other/number-input-stepper.svelte";
    import      KeyValueListItem   from "../components/list-items/key-value-list-item.svelte";
    import      Button             from "@v2/lib/components/buttons/button.svelte"
    import      BottomSheet        from "../components/_other/bottom-sheet.svelte";


    let {
        is_open = $bindable(),
        volume  = $bindable(),
        volumes,
        next_volume,
    }: {
        is_open?:    boolean
        volume?:     number | null
        volumes:     VolumeLabel[]
        next_volume: number
    } = $props()

    let draft_volume = $derived.by(() => 
    {
        is_open

        return volume ? volume : next_volume ?? 0
    })

    const already_exists = $derived(volumes.find(it => it.volume === draft_volume))
</script>



<BottomSheet 
    bind:is_open
    policy = "fit-expand"
>
    {#snippet header()}
        <input-wrapper>
            <NumberInputStepper 
                bind:value = { draft_volume }
                min_value  = { 1 }
            />

            <p 
                class           = "error-message"
                class:invisible = { !already_exists }
            >
                Volume already exists.
            </p>
        </input-wrapper>
    {/snippet}

    <sheet-wrapper
        class:center = { volumes.length === 0 }
    >
        {#if volumes.length > 0}
            <volume-list>
                {#each volumes as volume (volume.id)}
                    <KeyValueListItem 
                        key   = { volume.title }
                        value = "#{volume.volume}"
                    />
                {/each}
            </volume-list>
        {:else}
            <p class = "no-books-message">There are no other books in this series.</p>
        {/if}
    </sheet-wrapper>

    {#snippet footer()}
        <Button 
            state   = { already_exists ? "disabled" : "default" }
            onclick = { () => { volume = draft_volume; is_open = false } }
        >
            Set volume number
        </Button>
    {/snippet}
</BottomSheet>


<style>
    sheet-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        min-height: 128px;

        &.center
        {
            justify-content: center;
        }
    }

    input-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;

        .error-message
        {
            font-size: var(--text-caption);
            color: var(--text-error);
            margin-bottom: -12px;

            &.invisible
            {
                visibility: hidden;
            }
        }
    }

    :global(.other-volumes-list)
    {
        width: 100%;
    }

    volume-list
    {
        display: flex;
        flex-direction: column;
    }

    .no-books-message
    {
        color: var(--text-placeholder);
        text-align: center;
        margin-bottom: 32px;
    }
</style>