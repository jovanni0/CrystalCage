<script lang="ts">
    import BookOpen from "./icons/book-open.svelte";
</script>



<div class="cover-placeholder">
    <BookOpen 
        width={32} 
        height={32}
    />
</div>



<style>
    .cover-placeholder {
        width: 100%;
        height: 100%;

        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border-radius: 8px;

        background-color: var(--placeholder);
        color: var(--text-secondary);
    }
</style>