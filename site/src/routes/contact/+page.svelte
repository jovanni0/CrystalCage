<script>
    import SectionLeft from "./section-left.svelte";
    import SectionRight from "./section-right.svelte";

</script>




<page-wrapper>
    <SectionLeft />
    <SectionRight />
</page-wrapper>



<style>
    page-wrapper
    {
        display: grid;
        grid-template-columns: 40% 60%;
    }


@media (width <= 1000px)
{
    page-wrapper
    {
        grid-template-columns: 1fr;
        grid-template-rows: auto 1fr;
    }
}
</style>