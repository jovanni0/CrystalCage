<script lang="ts">
    import FloatingActions from "#lib/elements/floating-actions.svelte";
    import type { Snippet } from "svelte";

    let {
        children
    }: {
        children?: Snippet
    } = $props()
</script>




{@render children?.()}
<FloatingActions />