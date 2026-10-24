<script lang="ts">
    import type { HomeContent_WhySection } from "#lib/content/home-content.ts";
    import "#lib/styles/prose.css"

    let {
        content
    }: {
        content: HomeContent_WhySection
    } = $props()
</script>



<why-section>
    <p class = "title">{ content.title }</p>
    <doc-body>{@html content.body}</doc-body>
</why-section>



<style>
    why-section
    {
        display: flex;
        flex-direction: column;
        align-items: center;

        gap: 48px;
        padding: 64px 20px 96px 20px;

        background-color: var(--bg-surface-accent);
        color: var(--text-on_strong-default);

        p.title
        {
            font-size: var(--text-heading-md);
            font-weight: 600;
            text-align: center;
        }
    }
</style>