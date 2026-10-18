<script lang="ts">
    import type { HomeContent_StartSection } from "#lib/content/home-content.ts";
    import { ICON_REPOSITORY } from "#lib/repositories/icon-repository.ts";

    let {
        content
    }: { 
        content: HomeContent_StartSection
    } = $props()
</script>



<start-section>
    <p class = "title">{ content.title }</p>

    <option-wrapper>
        {#each content.items as item (item.title)}
            {@const Icon = ICON_REPOSITORY[item.icon]}
            <option-item>
                <Icon 
                    width = { 48 } 
                    height = { 48 } 
                />

                <p class = "title">{ item.title }</p>
                <p class = "text">{ item.text }</p>

                <a
                    href  = { item.action.href }
                    class = "button-mimic fat primary"
                >
                    { item.action.label }
                </a>
            </option-item>
        {/each}
    </option-wrapper>
</start-section>



<style>
    start-section
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

        option-wrapper
        {
            display: flex;
            flex-direction: row;
            justify-content: center;
            align-items: flex-start;

            gap: 48px;

            option-item
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

                a
                {
                    text-decoration: none;
                }
            }
        }
    }
</style>