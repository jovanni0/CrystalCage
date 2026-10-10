<script lang="ts">
    import CoverFrame from "@v2/lib/components/covers/cover-frame.svelte";



</script>



<page>
    <h1>Cover Image</h1>

    <grid>
        <CoverFrame status = "loading" />
        <CoverFrame status = "loaded" />
        <CoverFrame status = "empty" />
        <CoverFrame status = "error" />
    </grid>
</page>


<style>
    page
    {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        padding: 24px;
        background-color: var(--bg);
        box-sizing: border-box;
        overflow: scroll;
    }

    grid
    {
        display: grid;
        grid-template-columns: 200px 200px 200px 200px;
        column-gap: 16px;
    }
</style>