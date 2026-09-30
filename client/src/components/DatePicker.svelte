<script lang="ts">
    import Calendar from "@root/icons/Calendar.svelte";

    let {
        value = $bindable(null)   // ISO "yyyy-mm-dd" | null — matches your API's date string convention
    }: { 
        value?: string | null 
    } = $props()

    let text_value = $derived(value ? isoToDisplay(value) : "")
    let error: string | null = $state(null)
    let native_input: HTMLInputElement

    function isoToDisplay(iso: string): string
    {
        const [y, m, d] = iso.split("-")
        return `${d}/${m}/${y}` // dd/mm/yyyy — swap for whatever "sane" means to you
    }

    function displayToIso(text: string): string | null
    {
        const match = text.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/)
        if (!match) return null

        const [, d, m, y] = match
        const day = Number(d)
        const month = Number(m)
        const year = Number(y)
        const date = new Date(year, month - 1, day)

        const is_real_date = date.getFullYear() === year && date.getMonth() === month - 1 && date.getDate() === day
        if (!is_real_date) return null

        return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`
    }

    function handleInput()
    {
        if (text_value.trim() === "")
        {
            error = null
            value = null
            return
        }

        const iso = displayToIso(text_value)

        if (iso === null)
        {
            error = "Enter a valid date (dd/mm/yyyy)"
            return
        }

        error = null
        value = iso
    }

    function handleNativeChange()
    {
        value = native_input.value || null
        text_value = value ? isoToDisplay(value) : ""
        error = null
    }

    function formatDateInput(raw: string): string
    {
        const digits = raw.replace(/\D/g, "").slice(0, 8) // keep only digits, cap at ddmmyyyy = 8

        const day   = digits.slice(0, 2)
        const month = digits.slice(2, 4)
        const year  = digits.slice(4, 8)

        if (digits.length <= 2) return day
        if (digits.length <= 4) return `${day}/${month}`
        return `${day}/${month}/${year}`
    }
</script>

<div 
    class="date-entry"
    class:error={error !== null}
>
    <input
        type="text"
        inputmode="numeric"
        placeholder="dd/mm/yyyy"
        bind:value={text_value}
        oninput={(e) =>
        {
            text_value = formatDateInput(e.currentTarget.value)
            handleInput()
        }}
    >

    <button type="button" onclick={() => native_input.showPicker?.()}>
        <Calendar width = {20} height = {20} />
    </button>

    <input
        type="date"
        bind:this={native_input}
        onchange={handleNativeChange}
        class="visually-hidden"
        tabindex="-1"
    >
</div>

{#if error}
    <span class="error-message">{error}</span>
{/if}

<style>
    .date-entry {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 8px;
        flex: 1;
        background-color: var(--input-fill);
        border-radius: 4px;
        box-sizing: border-box;
        padding: 4px 8px;

        &.error
        {
            outline: 2px solid var(--red-primary);
        }
    }

    button
    {
        display: flex;
        align-items: center;
        color: var(--text-secondary);
    }

    input[type="text"] {
        all: unset;
        min-width: 0;
        width: 100%;
    }

    .visually-hidden {
        position: absolute;
        width: 1px;
        height: 1px;
        overflow: hidden;
        opacity: 0;
        pointer-events: none;
    }

    .error-message {
        color: var(--red-primary);
        font-size: small;
    }
</style>