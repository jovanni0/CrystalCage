<script lang="ts">
    import Button     from "@root/components/v2_redesign/Button.svelte";
    import InputField from "@root/components/v2_redesign/InputField.svelte";
    import Logo       from "@root/components/v2_redesign/Logo.svelte";
    import TopBar     from "@root/components/v2_redesign/TopBar.svelte";



    let {
        server_name,
        server_address,
        error_message,
        username        = $bindable(""),
        password        = $bindable(""),
        signup_url,
        is_loading,
        onlogin,
    }: {
        server_name:    string | null
        server_address: string
        error_message:  string
        username:       string
        password:       string
        signup_url:     string
        is_loading:     boolean
        onlogin?:      () => void
    } = $props()


    const are_fields_filled = $derived(username && password)
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
                <h1 class = "heading">Log in</h1>
                <p class = "subheading">If you forgot the credentials, you need to ask the server admin to reset them for you.</p>
            </div>
            
            <div class = "inputs-block">
                <InputField 
                    bind:value   = { username }
                    label        = "Username"
                    type         = "text"
                    autocomplete = "username"
                />

                <InputField 
                    bind:value  = { password }
                    label       = "Password"
                    error       = { error_message }
                    type        = "password"
                    preallocate = { true }
                />
            </div>
        </div>

        <div class = "footer-block">
            <Button 
                variant = "PRIMARY"
                state   = { is_loading ? "LOADING" : are_fields_filled ? "DEFAULT" : "DISABLED" }
                onclick = { onlogin }
            >
                Log in
            </Button>

            <Button 
                variant = "GHOST-ACCENT"
                href    = { signup_url }
            >
                Sign up
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