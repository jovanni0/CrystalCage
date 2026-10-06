<script lang="ts">
    import Button from "../buttons/button.svelte";
    import IconButton from "../buttons/icon-button.svelte";
    import Ex from "../icons/ex.svelte";
    import "./topbar-styles.css"



    let {
        title,
        is_action_disabled = false,
        onCancel,
        onAction,
    }: {
        title:               string
        is_action_disabled?: boolean
        onCancel?:           () => void
        onAction?:           () => void
    } = $props()
</script>



<topbar-cancel-action class = "topbar-element">
    <IconButton 
        shape   = "square"
        label   = "cancel"
        onclick = { onCancel }
    >
        <Ex />
    </IconButton>

    <p class = "title">{ title }</p>

    <Button
        size    = "slim"
        state   = { is_action_disabled ? "disabled" : "default" }
        onclick = { onAction }
    >
        Save
    </Button>
</topbar-cancel-action>