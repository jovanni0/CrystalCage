<script lang="ts">
    import { ripple_effect } from "../buttons/ripple-effect";
    import CoverImage from "./cover-image.svelte";
    import "../buttons/ripple-effect.css"



    let {
        cover_id,
        book_id,
        alt = "",
        label
    }: {
        cover_id: string | null
        book_id:  string
        alt?:     string
        label:    string
    } = $props()
</script>



<a
    href = "/book/{book_id}"
    class = "ripple-container"
    {@attach ripple_effect}
>
    <CoverImage 
        cover_id = { cover_id }
        alt      = { alt }
    />
    <p>{ label }</p>
</a>



<style>
    a 
    {
        display: flex;
        flex-direction: column;
        gap: 4px;
        justify-content: center;
        text-align: center;
    }

    p
    {
        margin: 0;

        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        overflow: hidden;

        overflow-wrap: anywhere;
        font-size: var(--text-body-sm);
        line-height: 1.2;
    }
</style>