<script lang="ts">
    import { analytics_consent } from "#lib/states/analytics-consent-state.svelte.ts";
    import { onMount }           from "svelte";
    import AnalyticsConsent      from "#lib/elements/analytics-consent.svelte";
    import Header                from "#lib/elements/header.svelte";
    import Footer                from "#lib/elements/footer.svelte";
    import SearchDialog          from "#lib/elements/search-dialog.svelte";
    import "#lib/styles/tokens.css";
    import "#lib/styles/fonts.css";
    import "#lib/styles/base.css";
    import "#lib/styles/prose.css";
    import "#lib/styles/resets.css"


    
    let { data, children } = $props()


    onMount(() =>
    {
        analytics_consent.restore()
    })
</script>



<Header nav_items = { data.site.nav } />

<page-content>
    {@render children()}
</page-content>

<Footer />

<SearchDialog />

{#if analytics_consent.is_ready && analytics_consent.choice === null}
    <AnalyticsConsent 
        onAccept = { () => analytics_consent.accept() }
        onReject = { () => analytics_consent.reject() }
    />
{/if}



<style>
    :global(analytics-consent)
    {
        position: fixed;
        bottom: 0;
        right: 0;
        
        margin: 16px;
    }
</style>