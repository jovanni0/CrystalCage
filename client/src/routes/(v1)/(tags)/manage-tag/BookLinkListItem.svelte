<script lang="ts">
    import CoverImage       from "@root/components/CoverImage.svelte";
    import CircleX          from "@root/icons/CircleX.svelte";


    let {
        title,
        series,
        cover_id,
        onClick = undefined
    } : {
        title:    string
        series:   string | null
        cover_id: string | null
        onClick?: () => void
    } = $props()
</script>



<li>
    <CoverImage 
        source = { cover_id ? { kind: "REMOTE", id: cover_id} : null }
        alt    = { title }
    />

    <div class = "details">
        <span class = "title">{ title }</span>

        {#if series}
            <span class = "series">{ series }</span>
        {/if}
    </div>

    {#if onClick !== undefined }
        <button onclick = {onClick}>
            <CircleX />
        </button>
    {/if}
</li>



<style>

</style>