<script lang="ts">
    import type { Snippet } from "svelte";
    import      { nav }     from "@root/CompositionRoot";
    import      IconButton  from "../buttons/icon-button.svelte";
    import      ChevronLeft from "@root/icons/chevron-left.svelte";
    import "./topbar-styles.css"

    
    let {
        title,
        action,
    }: {
        title: string
        action: Snippet
    } = $props()
</script>



<topbar-back-action class = "topbar-element">
    <IconButton 
        shape   = "square"
        label   = "back"
        onclick = { () => nav.goBack("/") }
    >
        <ChevronLeft />
    </IconButton>

    <p class = "title">{ title }</p>

    {@render action()}
</topbar-back-action>



<style>
    topbar-back-action
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        padding: 0 4px;
    }

    p.title
    {
        font-size: var(--text-heading-sm);
        font-weight: 500;
        flex: 1;

        display: block;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
    }
</style>