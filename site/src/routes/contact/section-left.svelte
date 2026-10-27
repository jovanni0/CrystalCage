<script lang="ts">
    import RedirectCardsWrapper from "./redirect-cards-wrapper.svelte";

    
</script>



<section-left>
    <title-wrapper>
        <h1>Get in touch</h1>
        <p>Questions, feedback or ideas about Sanctum?</p>
    </title-wrapper>

    <RedirectCardsWrapper />
</section-left>



<style>
    section-left
    {
        display: grid;
        grid-template-rows: auto 1fr;

        gap: 64px;
        padding: 48px 64px;

        color: var(--text-on_strong-default);
        background-color: var(--bg-surface-accent);
    }


    h1
    {
        font-weight: 500;
    }


    p
    {
        font-size: var(--text-body-lg);
    }


@media (width <= 1350px)
{
    section-left
    {
        padding: 48px 32px;
    }
}


@media (width <= 1000px)
{
    section-left
    {
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr;

        padding: 32px 20px;
        gap: 32px;
    }
}


@media (width <= 600px)
{
    section-left
    {
        grid-template-columns: 1fr;
        grid-template-rows: auto 1fr;

        gap: 48px;
    }
}
</style>