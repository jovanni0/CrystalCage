<script lang="ts">
    import type { SearchEntry }  from "#lib/content/search-index.ts";
    import type { SearchResult } from "#lib/utils/search.ts";
    import      { goto }         from "$app/navigation";
    import      { search }       from "#lib/utils/search.ts";
    import      { search_state } from "#lib/states/search-state.svelte.ts";
    import      IconButton       from "#lib/components/buttons/icon-button.svelte";
    import      Ex               from "#lib/components/icons/ex.svelte";
    import      Search           from "#lib/components/icons/search.svelte";



    type IndexStatus = "idle" | "loading" | "ready" | "failed"


    let dialog: HTMLDialogElement
    let input:  HTMLInputElement

    let query:        string        = $state("")
    let active_index: number        = $state(0)
    let index_status: IndexStatus   = $state<IndexStatus>("idle")
    let entries:      SearchEntry[] = $state([])

    const results: SearchResult[] = $derived(index_status === "ready" ? search(entries, query) : [])
    const has_query: boolean = $derived(query.trim() !== "")


    $effect(() =>
    {
        if (search_state.is_open && !dialog.open)
        {
            dialog.showModal()
            input.select()
            loadIndex()
        }

        if (!search_state.is_open && dialog.open)
        {
            dialog.close()
        }
    })


    // a new query starts again from the first result
    $effect(() =>
    {
        query
        active_index = 0
    })


    async function loadIndex()
    {
        if (index_status === "loading" || index_status === "ready")
        {
            return
        }

        index_status = "loading"

        try
        {
            const response = await fetch("/search-index.json")

            if (!response.ok)
            {
                throw new Error(`Search index responded with ${ response.status }`)
            }

            entries = await response.json()
            index_status = "ready"
        }
        catch
        {
            index_status = "failed"
        }
    }


    function openResult(href: string)
    {
        search_state.close()
        goto(href)
    }


    function handleInputKeys(event: KeyboardEvent)
    {
        if (results.length === 0)
        {
            return
        }

        if (event.key === "ArrowDown")
        {
            event.preventDefault()
            active_index = (active_index + 1) % results.length
        }
        else if (event.key === "ArrowUp")
        {
            event.preventDefault()
            active_index = (active_index - 1 + results.length) % results.length
        }
        else if (event.key === "Enter")
        {
            event.preventDefault()
            openResult(results[active_index].entry.href)
        }
    }


    // Ctrl/⌘ + K anywhere, or "/" when not typing in a field
    function handleShortcut(event: KeyboardEvent)
    {
        const target = event.target as HTMLElement
        const is_typing = target.matches("input, textarea, select, [contenteditable], [contenteditable] *")

        const is_ctrl_k = event.key === "k" && (event.ctrlKey || event.metaKey)
        const is_slash = event.key === "/" && !is_typing

        if (is_ctrl_k || is_slash)
        {
            event.preventDefault()
            search_state.open()
        }
    }


    // the content fills the dialog, so a click on the dialog itself is a click on the backdrop
    function closeOnBackdrop(event: MouseEvent)
    {
        if (event.target === dialog)
        {
            search_state.close()
        }
    }


    function resultLabel(result: SearchResult): string
    {
        return result.entry.section ?? result.entry.page
    }
</script>



<svelte:window onkeydown = { handleShortcut } />

<dialog
    bind:this  = { dialog }
    class      = "search-dialog"
    aria-label = "Search"
    closedby   = "any"
    onclose    = { () => search_state.close() }
    onclick    = { closeOnBackdrop }
>
    <search-content>
        <search-bar>
            <Search />

            <input
                bind:this = { input }
                bind:value={ query }
                type                  = "text"
                placeholder           = "Search the docs"
                autocomplete          = "off"
                spellcheck            = "false"
                role                  = "combobox"
                aria-label            = "Search the docs"
                aria-expanded         = { results.length > 0 }
                aria-controls         = "search-results"
                aria-autocomplete     = "list"
                aria-activedescendant = { results.length > 0 ? `search-result-${ active_index }` : undefined }
                onkeydown             = { handleInputKeys }
            >

            <IconButton
                label   = "close search"
                shape   = "square"
                onclick = { () => search_state.close() }
            >
                <Ex />
            </IconButton>
        </search-bar>

        <search-body>
            {#if index_status === "failed"}
                <p class = "search-message">Search is not available right now.</p>
            {:else if !has_query}
                <p class = "search-message">Search the docs, the roadmap and the contact page.</p>
            {:else if index_status !== "ready"}
                <p class = "search-message">Loading…</p>
            {:else if results.length === 0}
                <p class = "search-message">No results for “{ query.trim() }”.</p>
            {/if}

            <ul
                id         = "search-results"
                role       = "listbox"
                aria-label = "Search results"
                hidden     = { results.length === 0 }
            >
                {#each results as result, index (result.entry.href)}
                    <li
                        id            = "search-result-{ index }"
                        role          = "option"
                        aria-selected = { index === active_index }
                    >
                        <a
                            class        = "search-result"
                            class:active = { index === active_index }
                            href         = { result.entry.href }
                            tabindex     = "-1"
                            onclick      = { () => search_state.close() }
                            onmousemove  = { () => active_index = index }
                        >
                            {#if result.entry.section}
                                <span class = "result-page">{ result.entry.page }</span>
                            {/if}

                            <span class = "result-title">{ resultLabel(result) }</span>

                            <span class = "result-snippet">
                                {#each result.snippet as part, part_index (part_index)}
                                    {#if part.is_match}
                                        <mark>{ part.text }</mark>
                                    {:else}
                                        { part.text }
                                    {/if}
                                {/each}
                            </span>
                        </a>
                    </li>
                {/each}
            </ul>
        </search-body>

        <search-hints aria-hidden = "true">
            <span><kbd>↑</kbd> <kbd>↓</kbd> to move</span>
            <span><kbd>Enter</kbd> to open</span>
            <span><kbd>Esc</kbd> to close</span>
        </search-hints>
    </search-content>
</dialog>



<style>
    :global(html:has(dialog.search-dialog[open]))
    {
        overflow: hidden;
    }


    dialog.search-dialog
    {
        box-sizing: border-box;
        width: min(640px, calc(100vw - 32px));
        max-width: none;
        max-height: 75vh;
        margin: 12vh auto auto auto;
        padding: 0;

        border: 1px solid var(--border-neutral-subtle);
        border-radius: 16px;
        background-color: var(--bg-surface-raised);
        color: var(--text-neutral-default);
        box-shadow: 0 8px 32px rgb(0 0 0 / 0.2);

        &[open]
        {
            display: flex;
        }

        &::backdrop
        {
            background-color: rgb(0 0 0 / 0.5);
            backdrop-filter: blur(4px);
        }

        @media (width <= 600px)
        {
            width: 100%;
            height: 100dvh;
            max-height: none;
            margin: 0;

            border: none;
            border-radius: 0;
        }
    }


    search-content
    {
        display: flex;
        flex-direction: column;

        flex: 1;
        min-height: 0;
    }


    search-bar
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 12px;

        padding: 4px 4px 4px 20px;

        border-bottom: 1px solid var(--border-neutral-subtle);
        color: var(--text-neutral-subtle);

        input
        {
            all: unset;

            flex: 1;
            min-width: 0;
            height: 48px;

            font-size: var(--text-body-lg);
            color: var(--text-neutral-default);

            &::placeholder
            {
                color: var(--text-neutral-faint);
            }
        }
    }


    search-body
    {
        display: block;

        flex: 1;
        min-height: 0;
        overflow-y: auto;
        overscroll-behavior: contain;

        padding: 8px;
    }


    p.search-message
    {
        padding: 24px 16px;

        text-align: center;
        color: var(--text-neutral-subtle);
    }


    ul
    {
        display: flex;
        flex-direction: column;
        gap: 2px;

        margin: 0;
        padding: 0;
        list-style: none;
    }


    a.search-result
    {
        display: flex;
        flex-direction: column;
        gap: 2px;

        padding: 10px 16px;
        border-radius: 12px;

        color: inherit;
        text-decoration: none;

        &.active
        {
            background-color: var(--bg-overlay-subtle);
        }

        span.result-page
        {
            font-size: var(--text-caption);
            color: var(--text-neutral-subtle);
        }

        span.result-title
        {
            font-weight: 600;
        }

        span.result-snippet
        {
            font-size: var(--text-body-sm);
            color: var(--text-neutral-subtle);
            overflow-wrap: anywhere;

            mark
            {
                background-color: var(--bg-brand-subtle);
                color: var(--text-brand-default);
                border-radius: 4px;
            }
        }
    }


    search-hints
    {
        display: flex;
        flex-direction: row;
        gap: 16px;

        padding: 10px 20px;

        border-top: 1px solid var(--border-neutral-subtle);

        font-size: var(--text-caption);
        color: var(--text-neutral-subtle);

        kbd
        {
            padding: 1px 6px;

            border: 1px solid var(--border-neutral-subtle);
            border-radius: 4px;

            font-family: inherit;
        }

        /* phones have no arrow keys or Escape */
        @media (hover: none)
        {
            display: none;
        }
    }
</style>
