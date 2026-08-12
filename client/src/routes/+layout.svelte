<script lang="ts">
	import { session_manager } from "@root/CompositionRoot";
	import { page }            from "$app/state";
	import { goto }            from "$app/navigation";



	const LOGIN_ROUTES = ["/login/select-server", "/login/select-account"]
	let { children } = $props();


	$effect( () =>
	{
		const status = session_manager.status
		const path = page.url.pathname

		if (status === "UNKNOWN")
		{
			return
		}

		const target_path =
			status === "NO_SERVER"   ? "/login/select-server" :
			status === "NEEDS_LOGIN" ? "/login/select-account" :
					                   "/dashboard"
		const is_path_correct = status === "AUTHENTICATED" ? !LOGIN_ROUTES.includes(path) : path === target_path

		if (!is_path_correct)
		{
			console.log(`redirecting due to session status ${status} from ${path} to ${target_path}`)
			goto(target_path, { replaceState: true })
		}
	})
</script>



<main>
    {@render children()}
</main>



<style>
    main {
        flex: 1;
        display: flex;
        flex-direction: column;
        height: 100vh;
    }
</style>