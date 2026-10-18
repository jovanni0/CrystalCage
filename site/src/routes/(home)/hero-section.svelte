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
        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 96px 0 0 0;

        text-align: center;

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
            display: flex;
            flex-direction: row;

            gap: 16px;
        }

        screenshot-wrapper
        {
            position: relative;

            display: flex;
            flex-direction: row;
            justify-content: center;
            align-items: flex-start;
            align-self: stretch;

            gap: 48px;
            padding: 32px 0 64px 0;
            box-sizing: border-box;

            img
            {
                width: 250px;
                height: auto;
                
                border-radius: 16px;
                border: 1px solid var(--text-placeholder);
                box-shadow: 4px 4px 4px 0 rgba(0, 0, 0, 0.2);
            }

            & > :nth-child(odd)
            {
                margin-top: 48px;
            }

            &::before
            {
                content: "";

                position: absolute;
                inset: auto 0 0 0;
                height: 66%;
                z-index: 0;

                background-color: var(--bg-brand-secondary-dark);
            }

            & > *
            {
                z-index: 1;
            }
        }
    }

    a
    {
        text-decoration: none;
    }
</style>