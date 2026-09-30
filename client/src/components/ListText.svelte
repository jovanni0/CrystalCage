<script lang="ts">
    let {
        key,
        value
    } : {
        key: string
        value: string
    } = $props()
</script>



<div class="entry">
    <span class="key">{key}</span>
    <span class = "value">{ value }</span>
</div>



<style>
    .entry {
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid var(--dividers);
        padding: 12px 8px;
        justify-content: space-between;
    }
</style>