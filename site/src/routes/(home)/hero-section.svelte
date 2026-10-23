<script lang="ts">
    import type { HomeContent_HeroSection } from "#lib/content/home-content.ts";
    import "#lib/components/buttons/button-styles.css"



    let {
        content
    }: {
        content: HomeContent_HeroSection
    } = $props()
</script>



<hero-section>
    <text-wrapper>
        <p class = "eyebrow">{ content.eyebrow }</p>
        <p class = "title">{ content.title}</p>
        <p class = "lead">{ content.lead }</p>

        <button-wrapper>
            {#each content.actions as action (action.href)}
                <a
                    href            = { action.href }
                    class           = "button-mimic fat"
                    class:primary   = { action.style === "primary" }
                    class:secondary = { action.style === "secondary" }
                >
                    { action.label }
                </a>
            {/each}
        </button-wrapper>
    </text-wrapper>
    
    <screenshot-wrapper>
        {#each content.screenshots as image (image.src)}
            <img
                src = { image.src }
                alt = { image.alt }
            />
        {/each}
    </screenshot-wrapper>
</hero-section>



<style>
    hero-section
    {
        display: grid;
        justify-items: center;

        gap: 48px;
        padding: 96px 20px 0 20px;

        text-align: center;

        text-wrapper
        {
            grid-row: 1;
            max-width: 640px;

            display: flex;
            flex-direction: column;

            gap: 48px;
            padding: 0 20px;

            p.eyebrow
            {
                font-size: var(--text-body-sm);
                font-weight: 600;
                color: var(--text-muted);
            }
    
            p.title
            {
                font-family: Lora;
                font-size: var(--text-display-lg);
                font-weight: 600;
    
                margin: -32px 0 -24px 0;
            }
    
            p.lead
            {
                font-size: var(--text-body-lg);
                max-width: 550px;
            }
    
            button-wrapper
            {
                display: grid;
                grid-template-columns: 1fr 1fr;
    
                gap: 8px;
    
                max-width: 100%;
            }
        }

        screenshot-wrapper
        {
            grid-row: 2;
            grid-column: 1;

            container-type: inline-size;

            display: grid;
            place-items: end center;
            width: min(100%, 1000px);

            padding: 64px 0;

            img
            {
                grid-area: 1/1;
                height: auto;

                border-radius: 16px;
                border: 1px solid var(--text-placeholder);
                box-shadow: 4px 4px 4px 0 rgba(0, 0, 0, 0.2);
            }

            & > :nth-child(1)
            {
                width: min(40cqw, 300px);
                translate: -30cqw -2cqw;
            }

            & > :nth-child(2)
            {
                width: min(46cqw, 350px);
                z-index: 2;
            }

            & > :nth-child(3)
            {
                width: min(40cqw, 300px);
                translate: 30cqw -2cqw;
            }
        }

        &::before
        {
            content: "";

            grid-row: 2;
            grid-column: 1;

            align-self: end;
            justify-self: stretch;

            height: 66%;
            margin-inline: -20px;

            background-color: var(--bg-brand-secondary-dark);
        }
    }

    a
    {
        text-decoration: none;
    }
</style>