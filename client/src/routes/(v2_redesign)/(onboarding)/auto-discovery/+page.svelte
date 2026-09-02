<script lang="ts">
    import type { DiscoveryResult }      from "@root/utils/ServerDiscovery";
    import      { fly }                  from "svelte/transition";
    import      { quintOut }             from "svelte/easing";
    import      { ServerDiscovery }      from "@root/utils/ServerDiscovery";
    import      { onMount }              from "svelte";
    import      { goto }                 from "$app/navigation";
    import      { resultToSearchParams } from "./-logic";
    import      Button                   from "@root/components/v2_redesign/Button.svelte";
    import      Radar                    from "./Radar.svelte";
    import      NavListItem              from "./NavCard.svelte";



    let is_animation_done: boolean           = $state(false)
    let servers:           DiscoveryResult[] = $state([])

    const radar_pings = $derived(servers.map(it => ({ id: it.host, name: it.name}) ))


    onMount( async () => 
    {
        const result = await ServerDiscovery.discoverServers()

        // redundant check because of page load-level redirect in case auto-discovery is not possible (web).
        if (result === null)
        {
            goto("/manual-discovery", { replaceState: true })

            return
        }

        servers = result
    })
</script>



<div class = "page">
    <div class = "content-block">
        <div class = "info">
            <h1 class = "heading">Looking for your library</h1>
            <p class = "subheading">Searching your local network for a Sanctum server. This won't take long.</p>
        </div>

        <Radar 
            servers = { radar_pings }
            style = { servers.length > 0 ? "width: 60%;" : "" }
        />

        <ul 
            class:expanded = { is_animation_done }
            class = "scrollbar-thin"
            ontransitionend = { (event) => event.propertyName === "height" ? servers.length > 0 ? is_animation_done = true : is_animation_done = false : undefined }
        >
            {#each servers as server, index (server.host)}
                <li 
                    in:fly = {{ y: 16, duration: 300, delay: index * 60, easing: quintOut }}
                >
                    <NavListItem 
                        title = { server.name }
                        note  = { server.host   }
                        href  = "/v2-login?{resultToSearchParams(server)}"
                    />
                </li>
            {/each}
        </ul>
    </div>

    <Button
        variant = "GHOST-ACCENT"
        href    = "/manual-discovery?reason=action"
    >
        Enter address manually
    </Button>
</div>



<style>
    :global(body)
    {
        background-color: var(--bg);
        font-family: Manrope;
    }

    .page
    {
        height: 100%;
        box-sizing: border-box;

        display: flex;
        flex-direction: column;

        padding: 24px;
    }

    .content-block
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        flex: 1;
        
        gap: 32px;
    }

    .info
    {
        display: flex;
        flex-direction: column;
        
        gap: 16px;
        
        text-align: center;
    }

    .heading
    {
        font-size: var(--text-heading-md);
        font-family: Lora;
    }

    .subheading
    {
        font-size: var(--text-body);
        
        color: var(--text-muted);
    }

    ul 
    {
        all: unset;
        
        align-self: stretch;

        height: 0;
        max-height: 30%;

        overflow-y: hidden;
        scrollbar-gutter: stable;

        transition: height 1s ease;

        &:has(li) 
        {
            height: 30%;
            
            overflow-y: hidden;
            
            transition: height 1s ease;
        }

        &.expanded
        {
            overflow-y: auto;
        }
    }

    li
    {
        all: unset;
    }
</style>