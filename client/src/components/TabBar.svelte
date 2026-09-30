<script lang="ts">
    type Tab = { 
        label: string
        value: string
    }


    let { 
        tabs,
        selected,
        onselect,
    } : { 
        tabs: Tab[]
        selected: string,
        onselect: (value: string) => void
    } = $props()
</script>



<div class="tab-bar">
    {#each tabs as tab (tab.label)}
        <button
            class:active = { selected === tab.value }
            onclick      = { () => onselect(tab.value) }
        >
            { tab.label }
        </button>
    {/each}

    <div class="indicator"></div>
</div>



<style>
    .tab-bar {
        display: flex;
        flex-direction: row;
        position: relative;
        padding: 0 8px;
        flex: 1;
        border-bottom: 1px solid var(--dividers);
    }

    button {
        flex: 1;
        text-align: center;
        padding: 12px 16px;
        text-decoration: none;
        color: var(--text);
        font-weight: 500;

        &.active {
            anchor-name: --active;
        }
    }

    .indicator {
        height: 2px;
        border-radius: 4px 4px 0 0;
        background-color: var(--text);

        position: absolute;
        position-anchor: --active;
        bottom: anchor(bottom);
        left: anchor(left);
        right: anchor(right);
        transition: all 0.2s ease;
        will-change: left, right; /* remove motion artifacts by promote the element to its own layer before the transition starts, 
                                     so it composites cleanly without touching the pixels behind it. */
    }
</style>