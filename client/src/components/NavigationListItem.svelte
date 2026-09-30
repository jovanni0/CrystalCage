<script lang="ts">
    import ChevronRight from "./icons/chevron-right.svelte";


    let {
        label,
        hint,
    } : {
        label: string,
        hint?: string
    } = $props()
</script>


<li>
    <a href = "a">
        <span class = "label">{ label }</span>
        <span class = "hint">{ hint }</span>
        <ChevronRight />
    </a>
</li>



<style>
    li
    {
        border-bottom: 1px solid var(--dividers);
        padding: 0.75rem 0.25rem 0.75rem 0.75rem;
        list-style: none;
    }

    a 
    {
        display: flex;
        flex-direction: row;
        gap: 0.5rem;
        align-items: center;
    }

    .label 
    {
        flex: 1;
    }

    .hint
    {        
        color: var(--text-secondary);
    }
</style>