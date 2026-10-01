<script lang="ts">
    import Button from "../buttons/button.svelte";
    import IconButton from "../buttons/icon-button.svelte";
    import Ex from "../icons/ex.svelte";
    import "./topbar-styles.css"



    let {
        title,
        onCancel,
        onAction,
    }: {
        title: string
        onCancel?: () => void
        onAction?: () => void
    } = $props()
</script>



<topbar-cancel-action class = "topbar-element">
    <IconButton 
        shape = "square"
        label = "cancel"
        onclick = { onCancel }
    >
        <Ex />
    </IconButton>

    <p class = "title">{ title }</p>

    <Button
        size = "slim"
        onclick = { onAction }
    >
        Save
    </Button>
</topbar-cancel-action>