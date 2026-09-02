<script lang="ts">
    import Button from "@root/components/v2_redesign/Button.svelte";
    import Logo   from "@root/components/v2_redesign/Logo.svelte";
</script>



<div class = "page">
    <img src="/welcome-background.png" alt="welcome background">

    <div class = "logo-container">
        <Logo 
            width  = { 64 } 
            height = { 64 }
        />

        <div>
            <h1>Sanctum</h1>
            <p>Your library. Your rules.</p>
        </div>
    </div>

    <Button 
        class = "button"
        href  = "/auto-discovery"
    >Let's begin</Button>
</div>



<style>
    .page
    {
        display: flex;
        flex-direction: column;
        position: relative;
        justify-content: space-between;
        
        height: 100%;
        box-sizing: border-box;
        padding: 128px 32px;

        color: var(--text-inverted);

        font-family: Lora;
    }

    img
    {
        position: absolute;
        inset: 0;

        width: 100%;
        height: 100%;
        object-fit: cover;
        z-index: -1;
    }

    .logo-container
    {
        display: flex;
        flex-direction: row;
        justify-content: center;
        align-items: center;

        gap: 16px;
    }

    h1
    {
        font-size: var(--text-display-lg);
        font-weight: 600;
        letter-spacing: 0.03em;
    }

    p
    {
        font-size: var(--text-heading-sm);
    }

    :global(.button)
    {
        font-family: Manrope;
        font-weight: 500;
        
        box-shadow: 4px 4px 4px 0 rgba(0 0 0 / 0.4);
    }
</style>