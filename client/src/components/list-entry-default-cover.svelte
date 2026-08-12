<script>
    import ChevronRight from "./icons/chevron-right.svelte";

</script>


<div class="default-cover">
    <span class="heading">COVERS</span>

    <button>
        <span class="none-added">None added. Tap to modify.</span>

        <ChevronRight />
    </button>
</div>



<style>
    .default-cover {
        display: flex;
        flex-direction: column;
    }

    .heading {
        padding: 0 8px;
        font-weight: 700;
        color: var(--text-secondary);
        letter-spacing: 0.05em;
    }

    button {
        display: flex;
        float: row;
        padding: 12px 0px 12px 8px;
        align-items: center;
    }

    .none-added {
        font-style: italic;
        color: var(--text-secondary);
        flex: 1;
    }
</style>