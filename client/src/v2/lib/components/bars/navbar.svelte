<script lang="ts">
    import { nav } from "@root/CompositionRoot";
    import ButtonNavbar from "../buttons/button-navbar.svelte";
    import LibraryBig from "../icons/library-big.svelte";
    import BookPlus from "../icons/book-plus.svelte";
    import Tags from "../icons/tags.svelte";
    import Settings from "../icons/settings.svelte";


</script>


<navbar>
    <ButtonNavbar
        label = "Library"
        onClick = { () => nav.goTo("/") }
    >
        <LibraryBig />
    </ButtonNavbar>

    <ButtonNavbar
        label = "Add Book"
        onClick = { () => nav.goTo("/add-book") }
    >
        <BookPlus />
    </ButtonNavbar>

    <ButtonNavbar
        label = "Tags"
        // onClick = { () => nav.goTo("/") }
    >
        <Tags />
    </ButtonNavbar>

    <ButtonNavbar
        label = "Settings"
        // onClick = { () => nav.goTo("/") }
    >
        <Settings />
    </ButtonNavbar>
</navbar>


<style>
    navbar
    {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
        padding: 8px 24px;
        border-top: 1px solid var(--text-placeholder);
    }
</style>