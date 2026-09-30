<script lang="ts">
    import Checkbox from "../boxes/checkbox.svelte";


    
    let {
        message,
        is_checked = $bindable(false),
    }: {
        message: string
        is_checked: boolean
    } = $props()
</script>



<checkable-message>
    <button
        onclick = { () => is_checked = !is_checked }
    >
        <p>{ message }</p>
        <Checkbox is_checked = { is_checked } />
    </button>
</checkable-message>



<style>
    checkable-message
    {
        display: flex;
        flex-direction: row;
        align-self: stretch;
        justify-content: end;
    }
    
    button
    {
        display: flex;
        flex-direction: row;
        align-items: center;

        padding: 12px 8px 12px 8px;
        gap: 8px;
    }

    p
    {
        flex: 1;
        text-align: right;
    }
</style>