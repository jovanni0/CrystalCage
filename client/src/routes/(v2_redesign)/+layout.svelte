<script lang="ts">
    import type { Snippet } from "svelte";

    let { children }: { children?: Snippet } = $props()
</script>



<v2-wrapper>
    {@render children?.()}
</v2-wrapper>



<style>
    v2-wrapper
    {
        --bg:                  hsl(39, 47%, 94%);
        --bg-brand:            hsl(116, 21%, 32%);
        --bg-brand-disabled:   hsl(117, 15%, 70%);
        --bg-brand-secondary:  hsl(30, 42%, 39%);
        --bg-brand-pale:       hsl(65, 20%, 87%);
        --bg-inverted-muted:   hsl(33, 14%, 85%);
        --bg-overlay:          hsl(39, 47%, 98%);

        --text:                       hsl(33, 15%, 15%);
        --text-muted:                 hsl(34, 5%, 40%);
        --text-placeholder:           hsl(36, 5%, 60%);
        --text-brand:                 var(--bg-brand);
        --text-brand-disabled:        var(--bg-brand-disabled);
        --text-brand-secondary:       var(--bg-brand-secondary);
        --text-brand-secondary-muted: hsl(30, 35%, 60%);
        --text-error:                 hsl(6, 59%, 30%);
        --text-error-muted:           hsl(6, 15%, 50%);
        --text-inverted:              hsl(30, 15%, 95%);
        --text-inverted-muted:        hsl(33, 14%, 85%);
        

        --text-body:       16px;
        --text-body-lg:    18px;
        --text-body-sm:    14px;
        --text-caption:    12px;
        --text-display-lg: 48px;
        --text-display-sm: 36px;
        --text-heading-lg: 30px;
        --text-heading-md: 24px;
        --text-heading-sm: 20px;


        display: contents;
        font-family: Manrope;
    }

    :global(a)
    {
        -webkit-tap-highlight-color: transparent;
    }
</style>