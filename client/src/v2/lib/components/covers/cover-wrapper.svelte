<script lang="ts">
    import type { CoverType } from "@v2/lib/clients/api-covers";
    import      { ripple_effect } from "../buttons/ripple-effect";
    import      CoverImage from "./cover-image.svelte";
    
    import "../buttons/ripple-effect.css"



    let {
        cover_id,
        book_id,
        cover_type,
        alt = "",
        label
    }: {
        cover_id:   string | null
        book_id:    string
        cover_type: CoverType
        alt?:       string
        label:      string
    } = $props()
</script>



<a
    href = "/book/{book_id}"
    class = "ripple-container"
    {@attach ripple_effect}
>
    <CoverImage 
        cover_id   = { cover_id }
        cover_type = { cover_type }
        alt        = { alt }
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
        
        cursor: pointer;
        text-decoration: none;
        -webkit-tap-highlight-color: transparent;
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