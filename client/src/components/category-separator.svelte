<script lang="ts">
    import ChevronRight from "./icons/chevron-right.svelte";

    let {
        title,
        subtitle,
        href
    } : {
        title: string,
        subtitle?: string
        href?: string
    } = $props()
</script>



<a href={href} title="author">
    <span class="title">{title}</span>
    <span class="subtitle">{subtitle}</span>

    <ChevronRight/>
</a>



<style>
    a {
        display: flex;
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
        gap: 16px;
        align-self: 0 0 auto;
        padding-left: 8px;
        /* cursor: pointer; */
    }

    .title {
        font-weight: 500;
        font-size: large;
        flex: 1;
    }

    .subtitle {
        font-size: large;
        color: var(--text-secondary);
    }
</style>