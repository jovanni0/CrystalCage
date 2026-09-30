<script lang="ts">
    import Button     from "@root/components/v2_redesign/Button.svelte";
    import InputField from "@root/components/v2_redesign/InputField.svelte";
    import Logo       from "@root/components/v2_redesign/Logo.svelte";
    import TopBar     from "@root/components/v2_redesign/TopBar.svelte";



    let {
        server_name,
        server_address,
        username_error,
        signup_error,
        username       = $bindable(""),
        password       = $bindable(""),
        login_url,
        is_loading     = false,
        onSignUp,
    }: {
        server_name:    string | null
        server_address: string
        username_error: string
        signup_error:   string
        username:       string
        password:       string
        login_url:      string
        is_loading:     boolean
        onSignUp?:      () => void
    } = $props()


    let retype_password = $state("")

    const has_credentials:    boolean = $derived(username != "" && password != "" && retype_password != "")
    const do_passwords_match: boolean = $derived(password === retype_password)
    const password_error:     string  = $derived(do_passwords_match ? signup_error : "Passwords do not match." )
</script>



<div class = "page">
    <TopBar 
        title = {server_name ?? server_address}
        type  = "back"
    />

    <div class = "body-content">
        <div class = "content-block">
            <Logo style = "fill:var(--bg-brand-secondary); width:58px;" />

            <div class = "info">
                <h1 class = "heading">Sign up</h1>
                <p class = "subheading">Set the credentials for your account. Don't forget to also note them down.</p>
            </div>
            
            <div class = "inputs-block">
                <InputField 
                    bind:value = { username }
                    label      = "Username"
                    error      = { username_error }
                />

                <InputField 
                    bind:value = { password }
                    label      = "Password"
                    type       = "password"
                />

                <InputField 
                    bind:value  = { retype_password }
                    label       = "Re-type password"
                    error       = { password_error }
                    type        = "password"
                    preallocate = { true }
                />
            </div>
        </div>

        <div class = "footer-block">
            <Button 
                variant = "PRIMARY"
                state   = { is_loading ? "LOADING" : has_credentials === true && do_passwords_match === true ? "DEFAULT" : "DISABLED" }
                onclick = { onSignUp }
            >
                Sign up
            </Button>

            <Button 
                variant = "GHOST-ACCENT"
                href    = { login_url }
            >
                Log in
            </Button>
        </div>
    </div>
</div>



<style>
    :global(body)
    {
        background-color: var(--bg);

        font-family: Manrope;
    }

    .page
    {
        display: flex;
        flex-direction: column;

        height: 100%;
        box-sizing: border-box;

        padding: 24px 0 0 0;
    }

    .body-content
    {
        display: flex;
        flex-direction: column;
        flex: 1;
        
        padding: 0 24px 24px 24px;
    }

    .content-block
    {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        flex: 1;

        gap: 32px;
        padding: 0 16px;
    }

    .info
    {
        display: flex;
        flex-direction: column;
        text-align: center;

        gap: 16px;
    }

    .heading
    {
        font-size: var(--text-heading-md);
        font-family: Lora;
    }

    .subheading
    {
        font-size: var(--text-body);

        color: var(--text-muted);
    }

    .inputs-block
    {
        display: flex;
        flex-direction: column;
        align-self: stretch;

        gap: 8px;
    }

    .footer-block
    {
        display: flex;
        flex-direction: column;
    }
</style>