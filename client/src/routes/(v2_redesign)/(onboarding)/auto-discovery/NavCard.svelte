<script lang="ts">
    import ChevronRight from "@root/components/icons/chevron-right.svelte";



    let {
        title,
        note,
        href,
        level = 2,
    }: {
        title:  string
        note:   string
        href:   string
        level?: 1 | 2 | 3 | 4 | 5 | 6
    } = $props()
</script>



<a
    href = { href }
>
    <div class = "info">
        <svelte:element this = { `h${ level }` } >{ title }</svelte:element>
        <p>{ note }</p>
    </div>

    <ChevronRight />
</a>


<style>
    a
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;

        gap: 8px;
        padding: 16px 15px 16px 24px;
        margin: 2px;
    }

    .info
    {
        display: flex;
        flex-direction: column;

        gap: 4px;

        h1, h2, h3, h4, h5, h6
        {
            font-size: var(--text-body);
        }

        p
        {
            font-size: var(--text-caption);
            
            color: var(--text-muted);
        }
    }
</style>