<script lang="ts">
    let {
        text
    } : {
        text?: string
    } = $props()
</script>



<div class="chip">
    <span>{text}</span>
</div>



<style>
    .chip {
        padding: 8px 16px;
        border-radius: 100px;
        border: 1px solid var(--text-secondary);
        box-shadow: 0 0 2px 0 hsla(0 0% 0% / 0.25);
        flex: 0 0 auto;
    }
</style>