<script lang="ts">
	import { session }         from "@root/CompositionRoot";
	import { page }            from "$app/state";
	import { goto }            from "$app/navigation";
    import { onMount }         from "svelte";
    import { Capacitor }       from "@capacitor/core";
    import { App }             from "@capacitor/app"
    import { toSearchParams }  from "@root/utils/SearchParams";



	let { children } = $props();


    $effect(() =>
    {
        if (session.status === "NEEDS_LOGIN" && !page.route.id?.includes("onboarding"))
        {
            const search_params = toSearchParams({ 
                address: session.base_url, 
                name:    session.server_name,
            })

            goto(search_params ? `/login?${search_params}`: "/auto-discovery")
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