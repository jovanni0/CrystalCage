<script lang="ts">
    import type { HomeContent_FeaturesSection } from "#lib/content/home-content.ts";
    import { ICON_REPOSITORY } from "#lib/repositories/icon-repository.ts";


    let {
        content
    }: {
        content: HomeContent_FeaturesSection
    } = $props()
</script>


<features-section>
    <p class = "title">{ content.title }</p>

    <feature-wrapper>
        {#each content.items as feature (feature.title)}
            {@const Icon = ICON_REPOSITORY[feature.icon]}
            <feature-item>
                <Icon 
                    width = { 48 } 
                    height = { 48 } 
                />

                <text-wrapper>
                    <p class = "feature-title">{ feature.title }</p>
                    <p class = "text">{ feature.text }</p>
                </text-wrapper>
            </feature-item>
        {/each}
    </feature-wrapper>
</features-section>



<style>
    features-section
    {
        container-type: inline-size;

        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 64px 20px 96px 20px;

        p.title
        {
            font-size: var(--text-heading-md);
            font-weight: 600;
            text-align: center;
        }

        feature-wrapper
        {
            display: grid;
            grid-template-columns: repeat(4, 1fr);

            gap: 48px;
            
            
            @container (width < 1104px)
            {
                grid-template-columns: repeat(2, 1fr);
            }
            
            @container (width < 528px)
            {
                grid-template-columns: repeat(1, 1fr);
                justify-items: center;
                align-self: stretch;
            }

            feature-item
            {
                width: 240px;
                height: auto;

                display: grid;
                grid-template-columns: 1fr;
                justify-items: center;

                gap: 24px;

                @container (width < 528px)
                {
                    grid-template-columns: auto 1fr;
                    width: min(100%, 360px);
                    align-items: center;
                }

                & > :global(svg)
                {
                    color: var(--text-brand-secondary);
                    stroke-width: 1.5;
                }

                text-wrapper
                {
                    display: flex;
                    flex-direction: column;

                    gap: 12px;

                    text-align: center;

                    @container (width < 528px)
                    {
                        text-align: start;
                        align-self: stretch;
                        gap: 8px;
                    }

                    p.feature-title
                    {
                        font-size: var(--text-body-lg);
                        font-weight: 600;
                    }
    
                    p.text
                    {
                        color: var(--text-muted);
                    }
                }

            }
        }
    }
</style>