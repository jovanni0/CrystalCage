<script lang="ts">
    let {
        key,
        value
    }: {
        key:   string
        value: string
    } = $props()
</script>



<key-value-list-item>
    <p class = "key">{ key }</p>
    <p class = "value">{ value }</p>
</key-value-list-item>



<style>
    key-value-list-item
    {
        display: flex;
        flex-direction: row;
        gap: 16px;
        justify-content: space-between;
        padding: 12px 8px;
        border-bottom: 1px solid var(--text-placeholder);
    }

    .key
    {
        text-align: start;
    }

    .value
    {
        text-align: end;
    }
</style>