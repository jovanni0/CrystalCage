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

                <text-wrapper>
                    <p class = "option-title">{ item.title }</p>
                    <p class = "text">{ item.text }</p>
                </text-wrapper>

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
        container-type: inline-size;

        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 64px 0 96px 0;

        p.title
        {
            font-size: var(--text-heading-md);
            font-weight: 600;
            text-align: center;
        }

        option-wrapper
        {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            
            gap: 48px;
            
            @container (width < 528px)
            {
                grid-template-columns: 1fr;
                justify-items: center;
            }

            option-item
            {
                width: 240px;
                height: auto;

                display: grid;
                grid-template-columns: 1fr;
                align-items: center;
                justify-items: center;

                gap: 24px;


                @container (width < 528px)
                {
                    grid-template-columns: auto 1fr;
                    grid-template-rows: 1fr auto;
                    justify-items: initial;

                    width: min(100%, 360px);
                }

                & > :global(svg)
                {
                    color: var(--text-accent-default);
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
                        gap: 8px;
                        text-align: start;
                    }

                    p.option-title
                    {
                        font-size: var(--text-body-lg);
                        font-weight: 600;
                    }
    
                    p.text
                    {
                        color: var(--text-neutral-subtle);
                    }
                }

                a
                {
                    text-decoration: none;

                    @container (width < 528px)
                    {
                        grid-row: 2;
                        grid-column: span 2;
                    }
                }
            }
        }
    }
</style>