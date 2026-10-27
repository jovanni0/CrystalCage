<script lang="ts">
    import Button from "#lib/components/buttons/button.svelte";
    import InputArea from "#lib/components/input-area.svelte";
    import InputField from "#lib/components/input-field.svelte";
    import InputSelect from "#lib/components/input-select.svelte";
    import { ContactFormStore } from "#lib/stores/contact-form-store.svelte.ts";


    const store = new ContactFormStore()
</script>



<section-right>
    {#if store.status !== "sent"}
        <form-wrapper>
            <InputField 
                bind:value   = { store.name }
                error        = { store.name_error ?? undefined }
                label        = "Name" 
                is_mandatory = { true } 
                preallocate  = { true }
            />

            <InputSelect 
                bind:value  = { store.topic }
                options     = { store.topic_data }
                label       = "Topic"
                preallocate = { true }
            />

            <InputField 
                bind:value   = { store.email }
                error        = { store.email_error ?? undefined }
                label        = "Email" 
                is_mandatory = { true } 
                preallocate  = { true }
            />

            <InputField 
                bind:value  = { store.phone }
                error       = { store.phone_error ?? undefined }
                label       = "Phone number" 
                preallocate = { true }
            />

            <InputArea 
                bind:value   = { store.message }
                error        = { store.message_error ?? undefined }
                label        = "Message" 
                is_mandatory = { true } 
                class        = "message-input"
                preallocate  = { true }
            />

            <p class = "privacy-message">
                We only use your details to reply.
                <a href = ".." class = "privacy-link">Privacy&NonBreakingSpace;details.</a>
            </p>

            <button-wrapper>
                <Button 
                    class = "send-button"
                    state = { store.status === "sending" ? "loading" : "default" }
                    onclick = { () => store.submit() }
                >
                    Send
                </Button>

                {#if store.sending_error}
                    <p class = "send-error">{ store.sending_error }</p>
                {/if}
            </button-wrapper>
        </form-wrapper>
    {:else}
        <success-wrapper>
            <p class = "success">Success!</p>
            <p class = "success-message">Your message has been sent.<br>We'll reply to <strong>{ store.email }</strong> as soon as we can.</p>
        </success-wrapper>
    {/if}
</section-right>



<style>
    section-right
    {
        display: flex;
        align-items: flex-start;
        justify-content: center;

        padding: 48px 64px;

        min-height: 100vh;
    }


    form-wrapper
    {
        display: grid;
        grid-template-columns: 1fr 1fr;
        grid-template-rows: repeat(4, auto);

        gap: 8px;

        max-width: 650px;
        flex: 1;
    }


    a.privacy-link
    {
        text-decoration: underline;
    }


    button-wrapper
    {
        display: flex;
        flex-direction: column;

        gap: 4px;
    }

    p.send-error
    {
        font-size: var(--text-body-sm);
        color: var(--text-danger-default);

        padding: 0 16px;
    }


    :global(input-area.message-input)
    {
        grid-column: span 2;
        min-height: 128px;
    }

    success-wrapper
    {
        display: flex;
        flex-direction: column;

        gap: 32px;
    }

    p.success
    {
        font-size: var(--text-display-sm);
    }

    p.success,
    p.success-message
    {
        text-align: center;
    }


@media (width <= 1350px)
{
    section-right
    {
        padding: 48px 32px;
    }
}


@media (width <= 1000px)
{
    section-right
    {
        padding: 32px 20px;
    }
}


@media (width <= 650px)
{
    form-wrapper
    {
        grid-template-columns: 1fr;
        grid-template-rows: repeat(7, auto);

        max-width: 450px;

        gap: 16px;
    }


    :global(input-area.message-input)
    {
        grid-column: span 1;
    }
}
</style>