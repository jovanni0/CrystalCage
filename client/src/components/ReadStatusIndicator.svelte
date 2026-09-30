<script lang="ts">
    import type { ReadStatus } from "@root/types/ReadStatus";

    let {
        status,
        read_count,
    } : {
        status: ReadStatus,
        read_count: number
    } = $props()


    function formatReadStatus(status: ReadStatus, read_count: number)
    {
        switch (status)
        {
            case "UNREAD":      return "Not Read"
            case "IN_PROGRESS": return "In Progress"
            case "DNF":         return "Unfinished"
            case "READ":        return `Finished ${read_count > 0 ? `(${read_count})` : ""}`
            default:            return status satisfies never
        }
    }
</script>



<div 
    class          = "read-status-indicator"
    class:unread   = { status === "UNREAD" }
    class:progress = { status === "IN_PROGRESS" }
    class:dnf      = { status === "DNF" }
    class:read     = { status === "READ" }
>
    <span>{ formatReadStatus(status, read_count) }</span>
</div>



<style>
    .read-status-indicator 
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 12px;
        padding: 0.5rem 1.25rem;
        border-radius: 100px;
        flex: 0;
        align-self: self-start;
        
        &.unread
        {
            background-color: var(--red-secondary);
            color: var(--red-primary);
        }

        &.progress
        {
            background-color: var(--amber-secondary);
            color: var(--amber-text);
        }

        &.dnf
        {
            background-color: var(--text-secondary);
            color: var(--text);
        }

        &.read
        {
            background-color: var(--green-background);
            color: var(--green-text);
        }
    }

    .read-status-indicator::before
    {
        content: "";
        width: 0.5rem;
        height: 0.5rem;
        border-radius: 50%;
        background-color: currentColor;
    }
</style>