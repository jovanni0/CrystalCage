<script lang="ts">
    import type { TagManagementStore } from '@root/stores/TagMangementStore.svelte.js';
    import TopbarBack from '@root/components/topbar-back.svelte';
    import BookLinkListItem from '../../BookLinkListItem.svelte';
    import { marked } from 'marked';
    import Pencil from '@root/icons/Pencil.svelte';


    let { data } = $props()
    const { 
        tag_store, 
        id 
    } : { 
        tag_store: TagManagementStore
        id: string | undefined 
    } = $derived(data)
</script>



<TopbarBack
    page_name = { tag_store.name ?? "" }
>
    {#snippet right()}
        <a href = { `/manage-tag/${id}` }>
            <Pencil />
        </a>
    {/snippet}
</TopbarBack>

<div class = "content">
    <div
        class       = "description"
        class:unset = { tag_store.description === null }
    >
        {@html marked(tag_store.description ?? "Description not set")}
    </div>

    <div class = "links">
        <span class = "usage">{ tag_store.link_count > 0 ? `Used by ${ tag_store.link_count } books` : `Not used by any books yet` }</span>

        <ul class = "books">
            {#each tag_store.book_linked as link (link.book_id)}
                <BookLinkListItem 
                    title = { link.book_title }
                    series = { link.series_name }
                    cover_id = { link.cover_id }
                />
            {/each}
        </ul>
    </div>
</div>



<style>
    a
    {
        width: 48px;
        height: 48px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .content 
    {
        display: flex;
        flex-direction: column;
        gap: 32px;
        padding: 16px;
    }

    .links
    {
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .usage
    {
        padding-left: 0.5rem;
        color: var(--text-secondary);
    }

    .books
    {
        list-style: none;
        margin: 0;
        padding: 0;
    }

    .description
    {
        padding: 0 0.5rem;
        
        &.unset
        {
            font-style: italic;
            color: var(--text-secondary);
        }
    }
</style>