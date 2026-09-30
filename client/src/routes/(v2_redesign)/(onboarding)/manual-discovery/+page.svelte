<script lang="ts">
    import { page }          from "$app/state";
    import { Network }       from "@root/utils/Network";
    import { UrlResolver }   from "@root/utils/UrlResolver";
    import { pingCandidate } from "./-logic";
    import Button            from "@root/components/v2_redesign/Button.svelte";
    import InputField        from "@root/components/v2_redesign/InputField.svelte";
    import Logo              from "@root/components/v2_redesign/Logo.svelte";
    import { toSearchParams } from "@root/utils/SearchParams";
    import { Capacitor } from "@capacitor/core";



    let debounce_timer: ReturnType<typeof setTimeout> | undefined = undefined
    let controller:     AbortController | undefined               = undefined
    
    let input_value:   string        = $state("")
    let error_message: string        = $state("")
    let resolved_url:  string | null = $state(null)
    let server_name:   string | null = $state(null)
    let is_checking:   boolean       = $state(false)
    
    const data_as_params:        string  = $derived(toSearchParams({ address: resolved_url, name: server_name }))
    const is_navigation_active:  boolean = $derived(page.url.searchParams.get("reason") === "action")
    const is_descovery_possible: boolean = $derived(Capacitor.isNativePlatform())


    $effect( () =>
    {
        const current_input = input_value
        
        clearTimeout(debounce_timer)
        controller?.abort()

        is_checking = false
        error_message = ""

        if (!current_input)
        {
            return
        }

        debounce_timer = setTimeout(async () =>
        {
            const validation = UrlResolver.validate(current_input)

            if (!validation.valid)
            {
                error_message = validation.reason === "invalid-ip"
                    ? "This IP address is not valid"
                    : "This is not a valid URL or IP address."
                return
            }

            is_checking = true
            controller = new AbortController()

            const results = await Promise.all(
                validation.candidates.map( async (it) => ({
                    url: it,
                    ...await pingCandidate(it, controller!.signal),
                }))
            )

            if (controller.signal.aborted) 
            {
                return
            }

            const successful = results.find(it => it.outcome.kind === "SUCCESS")

            if (successful)
            {
                server_name = successful.name
                error_message = ""
                resolved_url = successful.url
            }
            else
            {
                const outcomes = results.map(it => it.outcome)
                error_message = Network.messageFor(Network.pickMostRelevant(outcomes)!) ?? "Could not reach a server at this address"
            }

            is_checking = false
        }, 400)

        return () => clearTimeout(debounce_timer)
    })
</script>



<div class = "page">
    <div class = "content-block">
        <Logo style = "fill:var(--bg-brand-secondary); width:58px;" />

        <div class = "info">
            <h1 class = "heading">{ is_navigation_active ? "Manual search" : "Library not found" }</h1>
            <p class = "subheading">Check that you are connected to the same network and the API port is exposed.</p>
        </div>
        
        <InputField 
            bind:value  = { input_value }
            label       = "Address"
            placeholder = "https://example.com:7489"
            error       = { error_message }
            preallocate = { true }
            loading     = { is_checking }
        />
    </div>

    <div class = "footer-block">
        <Button 
            variant = "PRIMARY"
            state   = { resolved_url === null ? "DISABLED" : "DEFAULT" }
            href    = "/login?{data_as_params.toString()}"
        >
            Continue
        </Button>

        {#if is_descovery_possible}
            <Button 
                variant = "GHOST-ACCENT"
                href    = "/auto-discovery"
            >
                Retry auto-discovery
            </Button>
        {:else}
            <p class = "button-placeholder">Auto-discovery is unavailable on web.</p>
        {/if}

    </div>
</div>



<style>
    :global(body)
    {
        background-color: var(--bg);

        font-family: Manrope;
    }

    .page
    {
        display: flex;
        flex-direction: column;

        height: 100%;
        box-sizing: border-box;

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
        padding: 0 16px;
    }

    .info
    {
        display: flex;
        flex-direction: column;
        text-align: center;

        gap: 16px;
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

    .footer-block
    {
        display: flex;
        flex-direction: column;
    }

    .button-placeholder
    {
        text-align: center;
        padding: 16px 32px;
    }
</style>