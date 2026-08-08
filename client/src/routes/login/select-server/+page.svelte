<script lang="ts">
    import { goto } from "$app/navigation";
    import Button from "@components/button.svelte";
    import InputText from "@components/input-text.svelte";
    import {session_manager} from "@root/CompositionRoot.ts";


    let url = $state<string>("")
    let error = $state<string|undefined>(undefined)
    let is_url_invalid = $derived(url.length < 1)


    async function onContinue()
    {
        const is_alive = await session_manager.connectToServer(url)

        if (is_alive)
        {
            goto("/login/select-account")
        }
        else
        {
            error = "Could not reach server. Check your internet connection."
        }
    }
</script>



<div class="page">
    <div class="center">
        <div class="info">
            <span class="title">Select a server to connect</span>
            <span class="details">Enter the URL address of the server you want to connect to. If you do not know it or do not know what that means, asks the server’s admin.</span>
        </div>

        <InputText
                label="URL"
                usage="url"
                placeholder="http(s)://"
                bind:value={url}
                error={error}
        />
    </div>

    <Button
            text="Continue"
            onClick={onContinue}
            type="primary"
            is_disabled={is_url_invalid}
    />
</div>



<style>
    .page {
        width: 100%;
        height: 100%;
        padding: 32px;
        box-sizing: border-box;
        display: flex;
        flex-direction: column;
    }

    .center {
        flex: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        gap: 24px;
    }

    .info {
        display: flex;
        flex-direction: column;
        gap: 20px;
        padding: 16px 0;

        & .title {
            font-size: 1.5rem;
            font-weight: 600;
            text-align: center;
        }

        & .details {
            font-size: 0.9rem;
            color: var(--text-secondary);
            text-align: center;
        }
    }
</style>