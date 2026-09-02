<script lang="ts">
	import { session_manager } from "@root/CompositionRoot";
	import { page }            from "$app/state";
	import { goto }            from "$app/navigation";
    import { onMount }         from "svelte";
    import { Capacitor }       from "@capacitor/core";
    import { App }             from "@capacitor/app"
    import { toSearchParams }  from "@root/utils/SearchParams";



	let { children } = $props();


	$effect( () =>
	{
		const status = session_manager.status
		const is_onboarding = page.route.id?.includes("onboarding") ?? "false"


        if (status === "AUTHENTICATED" && is_onboarding) 
        {
            goto("/")
        } 
        else if (status === "NEEDS_LOGIN" && !is_onboarding) 
        {
            const search_params = toSearchParams({ 
                address: session_manager.server_address, 
                name:    session_manager.server_name,
            })

            if (search_params)
            {
                goto(`/login?${search_params}`)
            }
            else
            {
                goto("/auto-discovery")
            }
        } 
        else if (status === "NO_SERVER" && !is_onboarding) 
        {
            goto("/welcome")
        }
	})


    onMount( () =>
    {
        if (!Capacitor.isNativePlatform())
        {
            return
        }

        App.addListener("backButton", ({ canGoBack }) => 
        {
            if (canGoBack)
            {
                window.history.back()
            }
            else
            {
                App.exitApp()
            }
        })
    })
</script>



{@render children()}