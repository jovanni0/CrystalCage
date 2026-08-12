<script lang="ts">
    import { goBack } from "@root/utils/go-back";
    import ArrowLeft from "../icons/arrow-left.svelte";
    import TopbarButton from "./topbar-button.svelte";
</script>



<TopbarButton 
    onClick={goBack}
>
    {#snippet content()}
        <ArrowLeft />
    {/snippet}
</TopbarButton>