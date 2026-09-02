<script lang="ts">
    import { goBack } from "@root/utils/go-back";
    import Button     from "@root/components/v2_redesign/Button.svelte";
    import Logo       from "@root/components/v2_redesign/Logo.svelte";
</script>



<div class = "error-page">
    <div class = "content-block">
        <Logo style = "fill:var(--bg-brand-secondary); width:58px;" />

        <div class = "info">
            <h1 class = "heading">Address missing</h1>
            <p class = "subheading">The server address seems to be missing. Before anything, you need to go back and select it.</p>
        </div>
    </div>

    <Button 
        variant = "PRIMARY"
        state   = "DEFAULT"
        onclick = { goBack }
    >
        Go back
    </Button>
</div>



<style>
    :global(body)
    {
        background-color: var(--bg);

        font-family: Manrope;
    }

    .error-page
    {
        display: flex;
        flex-direction: column;
        
        height: 100%;
        box-sizing: border-box;

        padding: 24px;
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
</style>