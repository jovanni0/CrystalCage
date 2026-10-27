<script lang="ts">
    import type { ContentRoadmap_Staged } from "#lib/content/content-roadmap.ts";
    import TimelineMarker from "./timeline-marker.svelte";


    let {
        milestones
    }: {
        milestones: ContentRoadmap_Staged[]
    } = $props()
</script>



<milestone-list>
    {#each milestones as item (item.title)}
        <milestone-wrapper>
            <p class = "completion-date">{ item.completion_date }</p>
            
            <TimelineMarker is_completed = { item.completion_date !== undefined } />
            
            <milestone-card class:completed = { item.completion_date !== undefined }>
                <p class = "title">{ item.title }</p>
                <p class = "caption">{ item.text }</p>
            </milestone-card>
        </milestone-wrapper>
    {/each}
</milestone-list>



<style>
    milestone-list
    {
        --line-column: 24px;

        display: grid;
        grid-template-columns: 1fr var(--line-column) minmax(0, 600px) var(--line-column) 1fr;
        column-gap: 16px;
    }


    milestone-wrapper
    {
        display: grid;
        grid-column: 1 / -1;
        grid-template-columns: subgrid;
        grid-template-areas: "date line card . .";


        &:last-child > :global(timeline-marker::after)
        {
            display: none;
        }
    }

    
    p.completion-date
    {
        grid-area: date;

        justify-self: end;
        align-self: center;

        color: var(--text-neutral-subtle);
        font-size: var(--text-body-sm);
    }


    :global(timeline-marker)
    {
        grid-area: line;
    }


    milestone-card
    {
        grid-area: card;
        
        display: flex;
        flex-direction: column;
        
        gap: 8px;
        padding: 24px;
        margin: 16px 0;

        border-radius: 16px;
        border: 1px solid var(--border-neutral-default);

        background-color: var(--bg-overlay-subtle);


        &.completed
        {
            background-color: var(--bg-brand-subtle);
        }
    }


    p.title
    {
        font-size: var(--text-body-lg);
        font-weight: 500;
    }


    p.caption
    {
        color: var(--text-neutral-subtle);
    }


@media (width <= 1000px)
{
    milestone-list
    {
        grid-template-columns: var(--line-column) minmax(0, 600px) var(--line-column);
        column-gap: 8px;

        align-self: center;
    }


    milestone-wrapper
    {
        grid-template-rows: auto 1fr;
        grid-template-areas: "line date ." "line card .";
    }


    p.completion-date
    {
        justify-self: start;

        padding: 0 24px;
    }


    milestone-card.completed
    {
        margin: 4px 0 16px 0;
    }
}


@media (width <= 780px)
{
    milestone-list
    {
        grid-template-columns: var(--line-column) minmax(0, 600px);
    }
}
</style>