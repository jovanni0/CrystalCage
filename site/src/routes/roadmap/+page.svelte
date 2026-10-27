<script lang="ts">
    import type { ContentRoadmap } from "#lib/content/content-roadmap.ts";
    import MilestoneList from "./milestone-list.svelte";
    import MilestoneCard from "./milestone-list.svelte";



    let { data } = $props()

    const roadmap: ContentRoadmap = $derived(data.roadmap)
</script>



<page-content>
    <title-wrapper>
        <h1 class = "page-title">{ roadmap.title }</h1>
        <p class = "page-caption">{ roadmap.caption }</p>
    </title-wrapper>

    <ideas-wrapper>
        <h2 class = "ideas-title">{ roadmap.ideas.title }</h2>
        <p class = "ideas">{ roadmap.ideas.items.join(" • ") }</p>
    </ideas-wrapper>

    <MilestoneList milestones = { roadmap.staged } />
</page-content>



<style>
    page-content
    {
        display: flex;
        flex-direction: column;

        gap: 48px;
        padding: 64px 32px;
    }


    title-wrapper,
    ideas-wrapper
    {
        display: flex;
        flex-direction: column;

        gap: 8px;

        text-align: center;
    }

    
    h1.page-title
    {
        font-size: var(--text-display-lg);
        font-weight: 700;
    }


    h2.ideas-title
    {
        font-size: var(--text-heading-sm);
        font-weight: 600;
    }


@media (width <= 780px)
{
    page-content
    {
        padding: 64px 20px;
    }
}
</style>