<script lang="ts">
    import { goto } from "$app/navigation";
    import Button from "@components/button.svelte";
    import InputText from "@components/input-text.svelte";
    import TopbarBack from "@components/topbar-back.svelte";
    import {session_manager} from "@root/CompositionRoot";


    let username = $state<string>("")
    let password = $state<string>("")
    let error = $state<string|undefined>(undefined)


    async function onContinue()
    {
        const result = await session_manager.login(username, password)

        if (!result.success)
        {
            switch (result.error_reason)
            {
                case "NETWORK_ERROR":       error = "Could not reach the server. Check if you are connected to the internet."; break
                case "INVALID_CREDENTIALS": error = "Credentials are invalid. Check them and try again"; break
                default:                    error = "Application error"
            }

            return
        }

        error = undefined
        // goto("/dashboard")
    }
</script>



<div class="page">
    <TopbarBack page_name={session_manager.getServerName()} />

    <div class="body">
        <div class="center">
            <div class="info">
                <span class="title">Enter credentials</span>
                <span class="details">Enter the username and password the server admin has given you.</span>
            </div>

            <div class="inputs">
                <InputText
                        label="Username"
                        usage="username"
                        bind:value={username}
                        type="text"
                />

                <InputText
                        label="Password"
                        usage="password"
                        bind:value={password}
                        type="password"
                />

                {#if error}
                    <span class="error">{error}</span>
                {/if}
            </div>
        </div>

        <Button
                text="Continue"
                type="primary"
                onClick={onContinue}
        />
    </div>
</div>



<style>
    .page {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
    }

    .body {
        padding: 32px;
        padding-top: 0;
        display: flex;
        flex-direction: column;
        align-self: stretch;
        flex: 1;
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

    .inputs {
        display: flex;
        flex-direction: column;
        align-self: stretch;
        gap: 8px;
    }

    .error {
        font-size: 0.75rem;
        color: var(--red-text);
        padding-left: 8px;
    }
</style>