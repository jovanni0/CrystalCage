<script lang="ts">
    let {
        key = "Key",
        value = $bindable<number|undefined>(),
        onchange
    } : {
        key?: string
        value?: number
        onchange?: () => void
    } = $props()
</script>



<div class="entry">
    <span class="key">{key}</span>
    <input 
        class="value"
        class:none-set={value == undefined}
        type="number"
        placeholder="0"
        min="1"
        required
        onchange={onchange}
        bind:value={value}
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

    input[type="number"] {
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

    input[type="number"]::-webkit-inner-spin-button,
    input[type="number"]::-webkit-outer-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }

    .value {
        flex: 1;
    }

    .value.none-set {
        font-style: italic;
        color: var(--text-secondary);
    }
</style>