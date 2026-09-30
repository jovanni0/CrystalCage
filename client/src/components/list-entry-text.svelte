<script lang="ts">
    let {
        key = "Key",
        value = $bindable()
    } : {
        key?: string
        value?: string
    } = $props()
</script>



<div class="entry">
    <span class="key">{key}</span>
    <input 
        class="value"
        class:none-set={value === undefined}
        placeholder="(Not set)"
        bind:value={value}
        required
    >
</div>



<style>
    .entry {
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid var(--dividers);
        padding: 12px 8px;
        padding-right: 0;
    }

    .key {
        min-width: 128px;
    }

    input {
        all: unset;
        background-color: var(--input-fill);
        padding: 4px 8px;
        width: 100%;
        border-radius: 4px;
        color: var(--text);

        &:valid {
            color: var(--text);
            font-style: normal !important;
        }
    }

    .value {
        flex: 1;
    }

    .value.none-set {
        font-style: italic;
        color: var(--text-secondary);
    }
</style>