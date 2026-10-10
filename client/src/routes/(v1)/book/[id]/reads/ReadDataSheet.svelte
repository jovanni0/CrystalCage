<script lang="ts">
    import Button from "@root/components/button.svelte";
    import Checkbox from "@root/components/checkbox.svelte";
    import DatePicker from "@root/components/DatePicker.svelte";


    let { 
        start_date = $bindable<string | null>(),
        end_date   = $bindable<string | null>(),
        is_historical = $bindable<boolean>(false),
        did_not_finish = $bindable(false),
        allow_historical,
        onConfirm,
    } : {
        start_date: string | null
        end_date:   string | null
        is_historical: boolean
        did_not_finish: boolean
        allow_historical: boolean
        onConfirm: () => void
    } = $props()


    let dialog_el: HTMLDialogElement

    export function open() { dialog_el.showModal() }
    export function close() { dialog_el.close() }


    function toggleHistorical(value: boolean)
    {
        is_historical = value

        if (value)
        {
            start_date = null
            end_date = null
        }
        else
        {
            const date = new Date().toISOString().slice(0, 10)

            if (start_date === null) 
            {
                if (did_not_finish) start_date = date
                else start_date = ""
            }
            if (end_date === null)
            {
                if (did_not_finish) end_date = date
                else end_date = ""
            }
        }
    }


    function toggleDnf(value: boolean)
    {
        did_not_finish = value

        if (value && !is_historical)
        {
            const date = new Date().toISOString().slice(0, 10)

            if (!start_date)
            {
                start_date = date
            }

            if (!end_date)
            {
                end_date = date
            }

            console.log(start_date, end_date)
        }
    }
</script>



<dialog bind:this={dialog_el} class="sheet">
    <div class="handle"></div>

    <div class = "info">
        <span class = "heading">Start reading</span>
        <span class = "subheading">Give some details about your read</span>
    </div>


    <div class = "content">
        <div class = "date">
            <span>Start:</span>
            {#if start_date === null}
                <span>Unknown</span>
            {:else}
                <DatePicker bind:value = { 
                    () => start_date,
                    (value) => value === null ? start_date = "" : value === undefined ? start_date = null : start_date = value
                } />
            {/if}
        </div>

        <div class = "date">
            <span>Finish:</span>
            {#if end_date === null}
                <span>Unknown</span>
            {:else}
                <DatePicker bind:value = { 
                    () => end_date,
                    (value) => value === null ? end_date = "" : value === undefined ? end_date = null : end_date = value
                } />
            {/if}
        </div>

        {#if allow_historical }
            <label class = "check-row">
                <input 
                    class="visually-hidden"
                    type = "checkbox"
                    onchange = { (e) => toggleHistorical((e.currentTarget as HTMLInputElement).checked) }
                />
                <span>Historical</span>
                <Checkbox selected = { is_historical } />
            </label>
        {:else}
            <span>There is already a historical record</span>
        {/if}

            <label class = "check-row">
                <input 
                    class="visually-hidden"
                    type = "checkbox"
                    onchange = { (e) => toggleDnf((e.currentTarget as HTMLInputElement).checked) }
                />
                <span>Did Not Finish (DNF)</span>
                <Checkbox selected = { did_not_finish } />
            </label>
    </div>

    <Button 
        text = "Start reading"
        fill = { true }
        onClick = { onConfirm }
    />
</dialog>


<style>
    .sheet {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        top: auto;
        width: 100%;
        max-width: none;
        margin: 0;
        border: none;
        border-radius: 20px 20px 0 0;
        padding: 24px 16px;
        padding-top: 32px;
        background: var(--base);
        box-sizing: border-box;
    }

    .sheet[open] {
        display: flex;
        flex-direction: column;
        gap: 32px;
    }

    .sheet::backdrop {
        background: rgba(0, 0, 0, 0.4);
    }

    .handle {
        position: absolute;
        top: 12px;
        left: 50%;
        transform: translateX(-50%);
        width: 40px;
        height: 4px;
        border-radius: 2px;
        background: var(--dividers);
    }

    .info
    {
        display: flex;
        flex-direction: column;

        .heading
        {
            font-weight: 500;
        }

        .subheading
        {
            color: var(--text-secondary);
        }
    }

    .content
    {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    .date
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        padding: 0 8px;

        span
        {
            width: 80px;
        }
    }

    .check-row
    {
        display: flex;
        flex-direction: row;
        justify-content: flex-end;
        align-items: center;
        gap: 16px;
        padding: 0 8px;
    }

    .visually-hidden
    {
        display: none;
    }
</style>