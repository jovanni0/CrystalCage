<script lang="ts">
    import { goBack } from "@root/utils/go-back";
    import IconButton from "../buttons/icon-button.svelte";
    import ArrowLeft from "../icons/arrow-left.svelte";
    import "./topbar-styles.css"

    
    let {
        title
    }: {
        title: string
    } = $props()
</script>



<topbar-back class = "topbar-element">
    <IconButton 
        shape = "square"
        label = "back"
        onclick = { goBack }
    >
        <ArrowLeft />
    </IconButton>

    <p class = "title">{ title }</p>
</topbar-back>