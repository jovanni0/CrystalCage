<script lang="ts">
    import { goto, replaceState } from "$app/navigation";
    import      { page }              from "$app/state";
    import      { logIn }             from "../-logic";
    import      AddressMissingPage    from "../AddressMissingPage.svelte";
    import      InputCredentialsPage  from "./InputCredentialsPage.svelte";



    const search_params = $derived(page.url.searchParams)
    const server_address = $derived(search_params.get("address"))
    const server_name = $derived(search_params.get("name"))

    let username: string  = $state("")
    let password: string  = $state("")
    let error:    string  = $state("")
    let loading:  boolean = $state(false)


    async function log_in()
    {
        if (server_address === null)
        {
            return
        }

        loading = true

        try
        {
            const result = await logIn(server_address, server_name ?? server_address, username, password)

            error = result ?? ""

            if (!result)
            {
                goto("/", { replaceState: true })
            }
        }
        catch
        {
            error = "Could not log in."
        }

        loading = false
    }
</script>



{#if !server_address}
    <AddressMissingPage />
{:else}
    <InputCredentialsPage 
        server_name    = { server_name }
        server_address = { server_address }
        error_message  = { error }
        bind:username  = { username }
        bind:password  = { password }
        signup_url     = "/signup?{search_params.toString()}"
        is_loading     = { loading }
        onlogin        = { log_in }
    />
{/if}