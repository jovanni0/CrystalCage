<script lang="ts">
    import { page }                from "$app/state";
    import { attempt }             from "@v2/lib/utils/result";
    import { logIn }               from "../-logic";
    import { api_auth, 
             logger }              from "@root/CompositionRoot";
    import AddressMissingPage      from "../AddressMissingPage.svelte";
    import CheckAccessCodePage     from "./CheckAccessCodePage.svelte";
    import RegisterCredentialsPage from "./RegisterCredentialsPage.svelte";



    const search_params  = $derived(page.url.searchParams)
    const server_address = $derived(search_params.get("address"))
    const server_name    = $derived(search_params.get("name"))

    let access_code:          string  = $state("")
    let is_access_code_valid: boolean = $state(false)
    let access_code_error:    string  = $state("")
    let is_checking_code:     boolean = $state(false)

    let username:       string  = $state("")
    let password:       string  = $state("")
    let username_error: string  = $state("")
    let signup_error:   string  = $state("")
    let is_signing_up:  boolean = $state(false)
    

    async function checkCode()
    {
        if (server_address === null)
        {
            access_code_error = "ERROR: server address not set."

            return
        }

        is_checking_code  = true
        access_code_error = ""

        try
        {
            const result = await attempt(logger, "check invite code", () => api_auth.isInviteCodeValid(server_address, access_code))

            if (!result.ok)
            {
                access_code_error = result.error.kind === "network" ? "Can't reach the server." : "Something went wrong."
                return
            }

            is_access_code_valid = result.value
            if (!result.value) access_code_error = "This code is not valid."
        }
        finally
        {
            is_checking_code = false
        }
    }


    async function signup()
    {
        if (server_address === null)
        {
            signup_error = "ERROR: server address not set."
            return
        }

        is_signing_up  = true
        username_error = ""
        signup_error   = ""

        try
        {
            const created = await attempt(logger, "sign up", () => api_auth.signUp(server_address, { 
                code: access_code, 
                username, 
                password 
            }))

            if (!created.ok)
            {
                switch (created.error.kind)
                {
                    case "conflict": username_error = "This username is already taken.";                    break
                    case "invalid":  signup_error   = "Check your details. The password may be too short."; break
                    case "network":  signup_error   = "Can't reach the server.";                            break
                    default:         signup_error   = "Could not create an account."
                }
                return
            }

            const login_error = await logIn(server_address, server_name ?? server_address, username, password)

            if (login_error)
            {
                signup_error = "Account was created but failed to log in. Try logging in."
            }
        }
        finally
        {
            is_signing_up = false
        }
    }
</script>



{#if !server_address}
    <AddressMissingPage />
{:else if !is_access_code_valid}
    <CheckAccessCodePage 
        server_name      = { server_name }
        server_address   = { server_address }
        error_message    = { access_code_error }
        bind:access_code = { access_code }
        login_url        = "/login?{search_params.toString()}"
        is_loading       = { is_checking_code }
        onContinue       = { checkCode }
    />
{:else}
    <RegisterCredentialsPage 
        server_name    = { server_name }
        server_address = { server_address }
        username_error = { username_error }
        signup_error   = { signup_error }
        bind:username  = { username }
        bind:password  = { password }
        login_url      = "/login?{search_params.toString()}"
        is_loading     = { is_signing_up }
        onSignUp       = { signup }
    />
{/if}