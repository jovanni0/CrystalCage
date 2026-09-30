<script lang="ts">
    import { marked } from "marked";


    let {
        title,
        content,
        highlite = false,
    } : {
        title:    string
        content:  string
        highlite: boolean
    } = $props()
</script>



<div class = "text-block">
    <div class = "title">{ title.toUpperCase() }</div>

    <div 
        class = "holder"
        class:highlite = { highlite }
    >
        {@html marked(content)}
    </div>
</div>



<style>
    .text-block
    {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .title
    {
        font-weight: 700;
        color: var(--text-secondary);
        letter-spacing: 0.05em;
    }

    .holder
    {
        display: flex;
        flex-direction: column;
        line-height: 1.5rem;
        text-align: justify;

        &.highlite
        {
            background-color: var(--lowered);
            border-left: 0.5rem solid var(--primary-text);
            padding: 0 0.75rem;
        }
    }
</style>