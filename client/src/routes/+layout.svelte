<script lang="ts">
	import { session_manager } from "@root/CompositionRoot";
	import { page }            from "$app/state";
	import { goto }            from "$app/navigation";



	let { children } = $props();


	$effect( () =>
	{
		const status = session_manager.status
		const path = page.url.pathname


        if (status === "AUTHENTICATED" && path.startsWith("/login")) 
        {
            goto("/dashboard");
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