<script lang="ts">
	import { nav, 
             session }      from "@root/CompositionRoot";
	import { page }         from "$app/state";
	import { afterNavigate, goto }         from "$app/navigation";
    import { onMount }      from "svelte";
    import { Capacitor }    from "@capacitor/core";
    import { App }          from "@capacitor/app"
    import { SessionRules } from "@v2/lib/rules/session-rules";
    import { triggerSoundEffect } from "@v2/lib/android/sound-feedback-plugin";



	let { children } = $props();

    afterNavigate(navigation => nav.onNavigated(navigation))


    function onClick(event: MouseEvent)
    {
        if (!event.isTrusted) return

        if (event.target instanceof HTMLInputElement && event.target.closest("label") !== null)
        {
            return   // the browser's follow-up click from the label
        }

        const target = (event.target as Element).closest("button, a[href], label, [role='button'], summary")

        if (target === null) return

        triggerSoundEffect()
    }


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

        const back_listener = App.addListener("backButton", () => nav.onNativeBack())
        document.addEventListener("click", onClick, { capture: true })

        return () => back_listener.then(it => it.remove())
    })
</script>



{@render children()}