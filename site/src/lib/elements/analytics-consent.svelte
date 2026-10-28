<script lang="ts">
    import Button from "#lib/components/buttons/button.svelte";


    let {
        onAccept,
        onReject
    }: {
        onAccept: () => void
        onReject: () => void
    } = $props()
</script>



<analytics-consent>
    <text-wrapper>
        <p class = "title">Help improve this site?</p>
        <p class = "message">With your permission, this site uses Google Analytics to count visits and see which pages are read. Nothing will be  loaded until you consent to it.</p>
        <a class = "privacy-details" href = "..">Privacy details</a>
    </text-wrapper>

    <Button
        variant = "secondary"
        onclick = { onAccept }
    >
        Accept
    </Button>
    
    <Button
        variant = "secondary"
        onclick = { onReject }
    >
        Reject
    </Button>
</analytics-consent>



<style>
    analytics-consent
    {
        z-index: 10;
        
        max-width: 460px;

        display: grid;
        grid-template-columns: 1fr 1fr;
        grid-template-rows: 1fr auto;

        padding: 24px 32px;
        row-gap: 32px;
        column-gap: 12px;

        background-color: var(--bg-surface-raised);

        border-radius: 16px;
        border: 1px solid var(--border-neutral-subtle);

        box-shadow: 4px 4px 4px 0 rgba(0, 0, 0, 0.2);
    }


    text-wrapper
    {
        display: flex;
        flex-direction: column;

        gap: 16px;

        grid-row: 1;
        grid-column: span 2;
    }


    p.title
    {
        font-size: var(--text-heading-sm);
        font-weight: 700;
    }


    a.privacy-details
    {
        text-decoration: underline;
    }
</style>