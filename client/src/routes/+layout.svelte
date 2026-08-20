<script lang="ts">
	import { session_manager } from "@root/CompositionRoot";
	import { page }            from "$app/state";
	import { goto }            from "$app/navigation";
    import { onMount }         from "svelte";
    import { Capacitor }       from "@capacitor/core";
    import { App }             from "@capacitor/app"



	let { children } = $props();


	$effect( () =>
	{
		const status = session_manager.status
		const path = page.url.pathname


        if (status === "AUTHENTICATED" && path.startsWith("/login")) 
        {
            goto("/");
        } 
        else if (status === "NEEDS_LOGIN" && !path.startsWith("/login")) 
        {
            goto("/login/select-account");
        } 
        else if (status === "NO_SERVER" && !path.startsWith("/login")) 
        {
            goto("/login/select-server");
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