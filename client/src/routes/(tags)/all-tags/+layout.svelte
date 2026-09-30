<script lang="ts">
    import Button from "@root/components/button.svelte";
    import TopbarBackTabbed from "@root/components/topbars/TopbarBackTabbed.svelte";

    let { children } = $props()
    const has_tags: boolean = true
</script>



<TopbarBackTabbed 
    tabs = {[
        { label: "by Name", href: "/all-tags" },
        { label: "by Usage", href: "/all-tags/by-usage" },
    ]}
    title = "All Tags"
/>
<main>
    {#if has_tags === false}
        <div 
            class="content"
            class:no_content = { !has_tags }
        >
            <div class = "explanation">
                <span>No tags here yet.</span>
                <span>Tags let you categorise and filter your books  however makes sense to you — genres, moods,  themes, reading challenges, anything you like.</span>
            </div>
            
            <Button 
                text = "Create tag" 
                fill = { false } 
            />
        </div>
    {:else}
        {@render children()}
    {/if}
</main>



<style>
    main
    {
        display: flex;
        flex-direction: column;
        flex: 1;
    }

    .explanation
    {
        display: flex;
        flex-direction: column;
        text-align: center;
        gap: 0.5rem;
    }

    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;
        
        &.no_content
        {
            flex: 1;
            justify-content: center;
        }
    }
</style>