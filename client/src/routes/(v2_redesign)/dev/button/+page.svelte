<script lang="ts">
    import LinkButton   from "@v2/lib/components/buttons/link-button.svelte";
    import IconButton   from "@v2/lib/components/buttons/icon-button.svelte";
    import ArrowLeft    from "@v2/lib/components/icons/arrow-left.svelte";
    import Button       from "@v2/lib/components/buttons/button.svelte";



    const sizes    = ["fat", "slim"] as const
    const variants = ["primary", "ghost"] as const
    const shapes   = ["round", "square"] as const
    const button_states   = ["default", "loading", "disabled"] as const
    const link_button_states   = ["default", "disabled"] as const
</script>



<page>
    <h1>Button</h1>
    {#each sizes as size}
        {#each variants as variant}
            <h2>{ size } - { variant }</h2>

            <grid>
                {#each button_states as state}
                    <Button
                        size    = { size } 
                        variant = { variant }
                        state   = { state }
                    >
                        { state }
                    </Button>
                {/each}
            </grid>
        {/each}
    {/each}


    <h1>LinkButton</h1>
    {#each sizes as size}
        {#each variants as variant}
            <h2>{ size } - { variant }</h2>

            <grid>
                {#each link_button_states as state}
                    <LinkButton
                        size    = { size } 
                        variant = { variant }
                        state   = { state }
                    >
                        { state }
                    </LinkButton>
                {/each}
            </grid>
        {/each}
    {/each}


    <h1>IconButton</h1>
    {#each shapes as shape}
        <h2>{ shape }</h2>

        <grid>
            {#each link_button_states as state}
                <IconButton
                    label   = "icon-button"
                    shape   = { shape }
                    state   = { state }
                >
                    <ArrowLeft />
                </IconButton>
            {/each}
        </grid>
    {/each}
</page>



<style>
    page
    {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        padding: 24px;
        background-color: var(--bg);
        box-sizing: border-box;
        overflow: scroll;
    }

    grid
    {
        display: grid;
        grid-template-columns: 200px 200px 200px;
        column-gap: 16px;
    }

    h2
    {
        padding-top: 32px;
        padding-bottom: 8px;
    }
</style>