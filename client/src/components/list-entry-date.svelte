<script lang="ts">
    import DatePicker from "./DatePicker.svelte";

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
    <div class = "picker">
        <DatePicker 
            bind:value={value}
        />
    </div>
    <!-- <input 
        class="value"
        class:none-set={!value}
        type="date"
        required
        bind:value={value}
    > -->
</div>



<style>
    .entry {
        display: flex;
        flex-direction: row;
        border-bottom: 1px solid var(--dividers);
        padding: 12px 8px;
        padding-right: 0;
        align-items: center;
    }

    .key {
        min-width: 128px;
    }

    .picker
    {
        display: flex;
        flex-direction: column;
    }

    /* input[type="date"] {
        all: unset;
        background-color: var(--input-fill);
        padding: 4px 8px;
        width: 100%;
        border-radius: 4px;
        color: var(--text-secondary);

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
    } */
</style>