<script lang="ts">
    import InputField from "@root/components/v2_redesign/InputField.svelte";
    import BottomSheet from "../components/_other/bottom-sheet.svelte";
    import IconButton from "../components/buttons/icon-button.svelte";
    import ArrowDownToLine from "../components/icons/arrow-down-to-line.svelte";
    import Button from "../components/buttons/button.svelte";
    import LoadingCircle from "../components/icons/loading-circle.svelte";
    import LocalCoverImage from "../components/covers/local-cover-image.svelte";
    import Ex from "../components/icons/ex.svelte";
    import CoverImage from "../components/covers/cover-image.svelte";
    import Trash from "../components/icons/trash.svelte";



    let {
        is_open = $bindable(),
        label   = $bindable(),
        error_message,
        cover_id,
        cover_file,
        resolution,
        format,
        is_source_enabled,
        is_main_button_enabled,
        onFetch,
        onUpload,
        onFinish,
        onClear,
        onDelete,
    }: {
        is_open:                boolean
        label:                  string
        error_message:          string
        cover_id:               string | null
        cover_file:             File | null
        resolution:             string | null
        format:                 string | null
        is_source_enabled:      boolean
        is_main_button_enabled: boolean
        onFetch:                (url: string) => void
        onUpload:               (file: File) => void
        onFinish:               () => void
        onClear:                () => void
        onDelete:               (id: string) => void
    } = $props()


    let file_input: HTMLInputElement

    let loading: boolean = $state(false)
    let source:  string  = $state("")


    async function onFetchFile()
    {
        loading = true
        void await onFetch(source)
        loading = false
    }

    function onPickFile()
    {
        const first_file = file_input.files ? file_input.files[0] : null

        if (first_file)
        {
            source = first_file.name

            onUpload(first_file)
        }

        file_input.value = ""
    }

    function onPasteFile(event: ClipboardEvent)
    {
        if (!is_source_enabled) return

        const pasted_files = Array.from(event.clipboardData?.files ?? [])
        const pasted_image = pasted_files.find(file => file.type.startsWith("image/"))

        if (!pasted_image) return

        event.preventDefault()
        source = "file:pasted image"

        onUpload(pasted_image)
    }

    function onClearSheet()
    {
        source = ""
        onClear()
    }

    async function onFinishActions()
    {
        loading = true
        await onFinish()
        loading = false
    }

    $effect( () =>
    {
        if (!is_open)
        {
            source = ""
        }
    })
</script>



<BottomSheet bind:is_open>
    {#snippet header()}
        {#if !cover_id}
            <header-wrapper>
                <InputField 
                    bind:value  = { source }
                    label       = "Source"
                    placeholder = "Paste a cover URL or image..."
                    readonly    = { !is_source_enabled }
                    error       = { error_message }
                    onpaste     = { onPasteFile }
                />
            </header-wrapper>
        {/if}
    {/snippet}


    <input
        type      = "file"
        accept    = "image/*"
        hidden
        bind:this = { file_input }
        onchange  = { onPickFile }
    />


    {#if cover_file !== null || cover_id !== null}
        <sheet-content>
            {#if cover_file}
                <LocalCoverImage file = { cover_file } />
            {:else}
                <CoverImage 
                    cover_id = { cover_id }
                    cover_type = "thumbnail"
                />
            {/if}
    
            <info-wrapper>
                <InputField
                    bind:value = { label }
                    label      = "Label"
                />
    
                <p class = "other-info">{ resolution } • { format?.toUpperCase() }</p>
            </info-wrapper>
        </sheet-content>
    {/if}


    {#snippet footer()}
        <buttons-wrapper>
            {#if cover_id}
                <IconButton
                    label = "delete cover"
                    shade = "danger"
                    onclick = { () => onDelete(cover_id) }
                >
                    <Trash />
                </IconButton>
            {:else}
                <IconButton 
                    label = "fetch image" 
                    state = { loading ? "disabled" : "default" }
                    onclick = { cover_file ? onClearSheet : onFetchFile } 
                >
                    {#if !loading && cover_file === null}
                        <ArrowDownToLine />
                    {:else if !loading && cover_file !== null}
                        <Ex />
                    {:else}
                        <LoadingCircle class = "spinner-icon"/>
                    {/if}
                </IconButton>
            {/if}

            <Button 
                class = "main-button" 
                state = { is_main_button_enabled && !loading ? "default" : "disabled" }
                onclick = { !cover_id && !cover_file ? () => file_input.click() : onFinishActions }
            >
                {#if cover_id !== null}
                    Update cover
                {:else if cover_file !== null}
                    Add cover
                {:else}
                    or select cover from device
                {/if}
            </Button>
        </buttons-wrapper>
    {/snippet}
</BottomSheet>



<style>
    header-wrapper
    {
        display: block;
        padding-bottom: 16px;
    }

    buttons-wrapper
    {
        display: flex;
        flex-direction: row;
        gap: 4px;
        padding-top: 16px;
    }

    :global(svg.spinner-icon)
    {
        animation: spin 1s linear infinite;
    }

    buttons-wrapper > :global(button.main-button)
    {
        flex: 1;
    }

    sheet-content
    {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 16px;
    }

    sheet-content > :global(cover-frame)
    {
        max-width: 65px;
        border-radius: 8px;
        overflow: hidden;
    }

    info-wrapper
    {
        display: flex;
        flex-direction: column;
        gap: 8px;
        flex: 1;
    }

    p.other-info
    {
        padding: 0 16px;
    }



    @keyframes spin
    {
        100%
        {
            transform: rotate(360deg);
        }
    }
</style>