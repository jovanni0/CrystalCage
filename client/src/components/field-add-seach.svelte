<script lang="ts">
    let {
        placeholder = "",
        value = $bindable("")
    } : {
        placeholder?: string
        value?: string
    } = $props()
</script>



<input
    bind:value={value}
    placeholder={placeholder}
>



<style>
    input {
        all: unset;
        padding: 12px 16px;
        border: 1px solid var(--text-secondary);
        border-radius: 8px;
        background-color: var(--lowered);
    }
</style>