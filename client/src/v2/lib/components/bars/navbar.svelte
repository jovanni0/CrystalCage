<script lang="ts" module>
    export type NavbarTabs = "library" | "authors" | "read-history" | "settings"
</script>



<script lang="ts">
    import ButtonNavbar   from "../buttons/button-navbar.svelte";
    import LibraryBig     from "../icons/library-big.svelte";
    import Settings       from "../icons/settings.svelte";
    import User           from "../icons/user.svelte";
    import RotateCcwClock from "../icons/rotate-ccw-clock.svelte";



    let {
        selected
    }: {
        selected: NavbarTabs
    } = $props()
</script>



<navbar>
    <ButtonNavbar
        label       = "Library"
        href        = "/"
        is_selected = { selected === "library" }
    >
        <LibraryBig />
    </ButtonNavbar>

    <ButtonNavbar
        label       = "Authors"
        href        = "/authors"
        is_selected = { selected === "authors" }
    >
        <User />
    </ButtonNavbar>

    <ButtonNavbar
        label       = "History"
        href        = "/read-history"
        is_selected = { selected === "read-history" }
    >
        <RotateCcwClock />
    </ButtonNavbar>

    <ButtonNavbar
        label       = "Settings"
        href        = "/settings"
        is_selected = { selected === "settings" }
    >
        <Settings />
    </ButtonNavbar>
</navbar>



<style>
    navbar
    {
        display: flex;
        flex-direction: row;

        background-color: color-mix(in srgb, var(--bg) 90%, black 10%);

        & > :global(a)
        {
            flex: 1;
        }
    }
</style>