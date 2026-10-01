<script lang="ts">
    import NumberInputStepper from "../components/_other/number-input-stepper.svelte";
    import CategoryListItem from "../components/list-items/category-list-item.svelte";
    import KeyValueListItem from "../components/list-items/key-value-list-item.svelte";
    import Sheetv2 from "./sheetv2.svelte";
    import Button from "@v2/lib/components/buttons/button.svelte"


    let {
        is_open = $bindable(),
        volume  = $bindable(),
        volumes,
        next_volume,
    }: {
        is_open?: boolean
        volume?:  number | null
        volumes:  { id: string, title: string, volume: number }[]
        next_volume: number
    } = $props()

    let draft_volume = $derived.by(() => 
    {
        is_open

        return volume ? volume : next_volume ?? 0
    })

    const already_exists = $derived(volumes.find(it => it.volume === draft_volume))
</script>



<Sheetv2 
    bind:is_open
    class = "sheet-body"
>
    <sheet-wrapper>
        <input-wrapper>
            <NumberInputStepper 
                bind:value = { draft_volume }
                min_value  = { 1 }
            />

            {#if already_exists}
                <p class = "error-message">Volume already exists.</p>
            {/if}
        </input-wrapper>
    
        <CategoryListItem
            title = "other volumes"
            class = "other-volumes-list"
        >
            <volume-list>
                {#each volumes as volume (volume.id)}
                    <KeyValueListItem 
                        key = { volume.title }
                        value = "#{volume.volume}"
                    />
                {/each}
            </volume-list>
        </CategoryListItem>
    </sheet-wrapper>

    {#snippet footer()}
        <Button 
            state = { already_exists ? "disabled" : "default" }
            onclick = { () => { volume = draft_volume; is_open = false } }
        >
            Set volume number
        </Button>
    {/snippet}
</Sheetv2>


<style>
    sheet-wrapper
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 32px;
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
</style>