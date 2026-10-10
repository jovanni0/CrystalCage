<script lang="ts">
    import Button     from "@root/components/v2_redesign/Button.svelte";
    import InputField from "@root/components/v2_redesign/InputField.svelte";
    import Logo       from "@root/components/v2_redesign/Logo.svelte";
    import TopBar     from "@root/components/v2_redesign/TopBar.svelte";



    let {
        server_name,
        server_address,
        error_message,
        access_code     = $bindable(""),
        login_url,
        is_loading      = false,
        onContinue,
    }: {
        server_name:    string | null
        server_address: string
        error_message:  string
        access_code:    string
        login_url:      string
        is_loading:     boolean
        onContinue?:    () => void
    } = $props()
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
                <p class = "subheading">Enter the code the server admin has given you.</p>
            </div>
            
            <InputField 
                bind:value  = { access_code }
                label       = "Access code"
                error       = { error_message }
                preallocate = { true }
            />
        </div>

        <div class = "footer-block">
            <Button 
                variant = "PRIMARY" 
                state   = { is_loading ? "LOADING" : access_code ? "DEFAULT" : "DISABLED" }
                onclick = { onContinue }
            >
                Continue
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

    .footer-block
    {
        display: flex;
        flex-direction: column;
    }
</style>