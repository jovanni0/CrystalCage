<script lang="ts">
    import BottomSheet from "../components/_other/bottom-sheet.svelte";
    import InputField from "../components/_other/input-field.svelte";
    import Button from "../components/buttons/button.svelte";
    import IconButton from "../components/buttons/icon-button.svelte";
    import FileUp from "../components/icons/file-up.svelte";
    import DateListItem from "../components/list-items/date-list-item.svelte";
    import KeyValueInsertListItem from "../components/list-items/key-value-insert-list-item.svelte";


    let {
        is_open = $bindable(),
        label = $bindable(),
        release_date = $bindable(),
        file_name,
        error_message,
        onSave,
    }: {
        is_open: boolean
        label: string
        release_date: string
        file_name: string
        error_message: string
        onSave: () => void
    } = $props()
</script>



<BottomSheet bind:is_open>
    {#snippet header()}
        <header-wrapper>
            <h1 class = "sheet-title">Add a new edition</h1>

            <InputField 
                bind:value = { label }
                label = "Label"
            />
        </header-wrapper>
    {/snippet}


    <DateListItem 
        bind:ISO_date = { release_date }
        key = "Release date"
    />
    <KeyValueInsertListItem key = "File name">
        <p class = "file-name">{ file_name }</p>
    </KeyValueInsertListItem>

    {#if error_message}
        <p class = "error-message">{ error_message }</p>
    {/if}



    {#snippet footer()}
        <buttons-wrapper class = "add-edition">
            <IconButton label = "upload file">
                <FileUp />
            </IconButton>

            <Button class = "primary-action" onclick = { onSave }>
                Add edition
            </Button>
        </buttons-wrapper>
    {/snippet}
</BottomSheet>



<style>
    h1.sheet-title
    {
        font-size: var(--text-body-lg);
        font-weight: 500;
        text-align: center;
    }


    buttons-wrapper.add-edition
    {
        display: flex;
        flex-direction: row;

        gap: 8px;
        padding-top: 16px;
    }


    buttons-wrapper > :global(button.primary-action)
    {
        flex: 1;
    }

    p.file-name
    {
        text-align: right;
        line-break: anywhere;
    }

    p.error-message
    {
        font-size: var(--text-caption);
        color: var(--red-primary);
    }
</style>