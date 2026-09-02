<script lang="ts">
    import { page }                from "$app/state";
    import { isInviteCodeValid, 
             logIn, 
             signUp }              from "../-logic";
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

        is_checking_code = true

        try
        {
            is_access_code_valid = await isInviteCodeValid(access_code, server_address)

            if (!is_access_code_valid)
            {
                access_code_error = "This code is not valid."
            }
        }
        catch
        {
            access_code_error = "This code is not valid."
        }

        is_checking_code = false
    }


    async function signup()
    {
        if (server_address === null)
        {
            signup_error = "ERROR: server address not set."

            return
        }
        
        is_signing_up = true

        try
        {
            const signup_response_status = await signUp(access_code, username, password, server_address)
    
            if (signup_response_status === 201)
            {
                try
                {
                    const login_error = await logIn(username, password, server_address)
        
                    if (login_error)
                    {
                        signup_error = "Account was created but failed to log in."
                    }
                }
                catch
                {
                    signup_error = "Account was created but failed to log in."
                }
            }
            else if (signup_response_status === 400)
            {
                username_error = "This username is already taken."
            }
            else
            {
                signup_error = "Could not create an account."
            }
        }
        catch
        {
            signup_error = "Could not create an account."
        }

        is_signing_up = false
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