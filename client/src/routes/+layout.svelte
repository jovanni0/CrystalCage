<script lang="ts">
	import { nav, 
             session }      from "@root/CompositionRoot";
	import { page }         from "$app/state";
	import { goto }         from "$app/navigation";
    import { onMount }      from "svelte";
    import { Capacitor }    from "@capacitor/core";
    import { App }          from "@capacitor/app"
    import { SessionRules } from "@v2/lib/rules/session-rules";



	let { children } = $props();


    // guards every navigation after the first load, and sessions that end while a page is open
    $effect(() =>
    {
        const target = SessionRules.redirectFor(session.status, page.route.id, session.base_url, session.server_name)

        if (target !== null)
        {
            goto(target, { replaceState: true })
        }
    })

    
    onMount( () =>
    {
        if (!Capacitor.isNativePlatform()) return

        const listner = App.addListener("backButton", () => nav.onNativeBack())

        return () => listner.then(it => it.remove())
    })
</script>



{@render children()}