<script lang="ts">
    import Bug from "#lib/components/icons/bug.svelte";
    import ChevronRight from "#lib/components/icons/chevron-right.svelte";
    import Lightbulb from "#lib/components/icons/lightbulb.svelte";
    import ServerOff from "#lib/components/icons/server-off.svelte";


</script>



<cards-wrapper>
    <p class = "contact-callout">Before you contact us</p>
    
    <intermittent-container>
        <a class = "redirect-card" href = "https://github.com/jovanni0/CrystalCage/issues">
            <Bug class = "main-icon" />

            <text-wrapper>
                <p class = "redirect-title">Found a bug?</p>
                <p class = "redirect-caption">Report it so it can be tracked and fixed.</p>
            </text-wrapper>

            <ChevronRight />
        </a>

        <a class = "redirect-card" href = "/roadmap">
            <Lightbulb class = "main-icon" />

            <text-wrapper>
                <p class = "redirect-title">Have an idea?</p>
                <p class = "redirect-caption">Suggest a feature or see what's planned.</p>
            </text-wrapper>

            <ChevronRight />
        </a>

        <a class = "redirect-card" href = "/docs/hosting">
            <ServerOff class = "main-icon" />

            <text-wrapper>
                <p class = "redirect-title">Stuck installing?</p>
                <p class = "redirect-caption">Report it so it can be tracked and fixed.</p>
            </text-wrapper>

            <ChevronRight />
        </a>
    </intermittent-container>
</cards-wrapper>


<style>
    cards-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    intermittent-container
    {
        display: flex;
        flex-direction: column;

        gap: 16px;
    }


    p.contact-callout
    {
        font-size: var(--text-heading-sm);
        font-weight: 500;
    }


    a.redirect-card
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 16px;
        padding: 32px 24px;

        background-color: rgba(255, 255, 255, 0.1);
        border: 1px solid var(--text-neutral-faint);
        border-radius: 16px;
    }


    a.redirect-card > :global(svg.main-icon)
    {
        width: 48px;
        height: 48px;
        stroke-width: 1.5;
    }


    text-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex: 1;
    }


    p.redirect-title
    {
        font-size: var(--text-heading-sm);
        font-weight: 600;
    }


    p.redirect-caption
    {
        color: var(--text-on_strong-subtile);
    }


@media (width <= 1000px)
{
    intermittent-container
    {
        background-color: rgba(255, 255, 255, 0.1);
        border: 1px solid var(--text-neutral-faint);
        border-radius: 16px;

        gap: 0;
    }


    a.redirect-card
    {
        background-color: transparent;
        border: none;
        border-radius: 0;

        padding: 12px;


        &:not(:last-child)
        {
            border-bottom: 1px solid var(--border-neutral-default);
        }
    }


    a.redirect-card > :global(svg.main-icon)
    {
        width: 24px;
        height: 24px;
        stroke-width: 2;
    }


    p.redirect-title
    {
        font-weight: 400;
    }
    

    p.redirect-caption
    {
        display: none;
    }
}
</style>