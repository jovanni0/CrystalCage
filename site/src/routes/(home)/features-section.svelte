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

                <p class = "title">{ feature.title }</p>
                <p class = "text">{ feature.text }</p>
            </feature-item>
        {/each}
    </feature-wrapper>
</features-section>



<style>
    features-section
    {
        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 64px 0 96px 0;

        p.title
        {
            font-size: var(--text-heading-md);
            font-weight: 600;
        }

        feature-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: center;
            align-items: flex-start;

            gap: 48px;

            feature-item
            {
                width: 240px;
                height: auto;

                display: flex;
                flex-direction: column;
                align-items: center;

                gap: 24px;

                & > :global(svg)
                {
                    color: var(--text-brand-secondary);
                    stroke-width: 1.5;
                }

                p.title
                {
                    font-size: var(--text-body-lg);
                    font-weight: 600;

                    margin-bottom: -12px;
                }

                p.text
                {
                    color: var(--text-muted);
                    text-align: center;
                }
            }
        }
    }
</style>