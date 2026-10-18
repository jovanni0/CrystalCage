<script lang="ts">
    import Header from "#lib/components/header.svelte";
    import Footer from "#lib/components/footer.svelte";
    import "#lib/styles/tokens.css";
    import "#lib/styles/fonts.css";
    import "#lib/styles/base.css";
    import "#lib/styles/prose.css";


    
    let { data, children } = $props()
</script>



<Header nav_items = { data.site.nav } />

<page-content>
    {@render children()}
</page-content>

<Footer />