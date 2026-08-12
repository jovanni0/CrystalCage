<script>
    import Search from "./icons/search.svelte"
</script>



<div class="searchbar">
    <input
        type="text"
        placeholder="Search library"
    />

    <Search />
</div>



<style>
    .searchbar {
        border: 1px solid var(--text-secondary);
        border-radius: 24px;
        padding: 10px 16px 10px 24px;
        display: flex;
        flex-direction: row;
        flex: 1;
        min-width: 16px;
    }

    input {
        all: unset;
        -webkit-appearance: none;
        appearance: none;
        min-width: 0;
        flex: 1;
        /* width: 0; */
    }
</style>